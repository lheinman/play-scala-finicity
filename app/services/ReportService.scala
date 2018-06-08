package services

import javax.inject.Inject
import models.{PersonRepository, Report, TokenRepository}
import play.api.Logger
import play.api.libs.json.{JsResult, JsValue, Json, Reads}
import play.api.libs.ws.{WSClient, WSRequest}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration

class ReportService @Inject() (
                                pRepo: PersonRepository,
                                tRepo: TokenRepository,
                                tService: TokenService,
                                customerService: CustomerService,
                                ws: WSClient)(implicit ec: ExecutionContext) {

  def patchReportsByCustomer(customer: String): Future[Option[Report]]  = {
    val finicityRequest: WSRequest = ws
      .url(url = s"https://api.finicity.com/decisioning/v1/customers/$customer/reports")
      .addHttpHeaders(hdrs = "Finicity-App-Key" -> sys.env("FINICITY_APP_KEY"))
      .addHttpHeaders(hdrs = "Finicity-App-Token" -> Await.result(tService.getToken, Duration.Inf))
      .addHttpHeaders(hdrs = "Accept" -> "application/json")

    finicityRequest.get().map {
      response => {
        if (response.status == 200) {
          Json.parse(response.body).validate[Reports].map{
            case reports => {
              reports.reports.map(report => {
                if (!Await.result(pRepo.isBorrowerByReport(report.id), Duration.Inf)) {
                  Await.result(pRepo.patchReport(customer, report.id, report.createdDate), Duration.Inf)
                  Some(report)
                } else None
              }).head
            }
          }.getOrElse(None)
        } else {
          Logger.debug(message = s"${response.status}: ${response.body}")
          None
        }
      }
    }
  }

  def patchReports: Future[String]  = {
    val finicityRequest: WSRequest = ws
      .url(url = "https://api.finicity.com/aggregation/v1/customers")
      .addHttpHeaders(hdrs = "Finicity-App-Key" -> sys.env("FINICITY_APP_KEY"))
      .addHttpHeaders(hdrs = "Finicity-App-Token" -> Await.result(tService.getToken, Duration.Inf))
      .addHttpHeaders(hdrs = "Accept" -> "application/json")

    finicityRequest.get().map {
      response => {
        if (response.status == 200) {
          Json.parse(response.body).validate[Customers].map{
            case customers => {
              customers.customers.map(customer => {
                val report = Await.result(patchReportsByCustomer(customer.id), Duration.Inf)
                if (report != None && !Await.result(pRepo.isBorrowerByReport(report.get.id), Duration.Inf)) {
                  Await.result(pRepo.patchReport(customer.id, report.get.id, report.get.createdDate), Duration.Inf)
                } else 0
              }).head
            }.toString
          }.getOrElse("Json.parse error")
        } else {
          s"${response.status}: ${response.body}"
        }
      }
    }
  }
}

case class Reports(reports: List[Report])

object Reports {
  implicit val readsReports: Reads[Reports] = new Reads[Reports] {
    def reads(json: JsValue): JsResult[Reports] = {
      for {
        reports <- (json \ "reports").validate[List[Report]]
      } yield Reports(reports)
    }
  }
}