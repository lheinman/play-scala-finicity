package services

import java.util.Date

import javax.inject.Inject
import models.{FinicityConnect, Person, PersonRepository, TokenRepository}
import play.api.Logger
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSRequest}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration

class FinicityService @Inject() (
                                  pRepo: PersonRepository,
                                  tService: TokenService,
                                  customerService: CustomerService,
                                  ws: WSClient)(implicit ec: ExecutionContext) {
  def postFinicityConnect(borrower: Person): Future[String] = {
    val request: WSRequest = ws
      .url(url = s"https://api.finicity.com/connect/v1/generate")
      .addHttpHeaders(hdrs = "Finicity-App-Key" -> sys.env("FINICITY_APP_KEY"))
      .addHttpHeaders(hdrs = "Finicity-App-Token" -> Await.result(tService.getToken, Duration.Inf))
      .addHttpHeaders(hdrs = "Accept" -> "application/json")


    val data = Json.obj(fields =
      "partnerId" -> sys.env("FINICITY_PARTNER_ID"),
      "customerId" -> borrower.customer,
      "consumerId" -> borrower.consumer,
      "redirectUri" -> "https://www.loandepot.com/",
      "type" -> "voa",
      "fromDate" -> ((new Date).getTime / 1000 - 60 * 24 * 3600),
      "webhook" -> sys.env("FINICITY_WEBHOOK"),
      "webhookContentType" -> "application/json")
    request.post(data).map { response =>
      if (response.status == 201) {
        Json.parse(response.body).validate[FinicityConnect].map {
          case link => {
            link.link
          }
        }.getOrElse("Json.parse error")
      } else {
        Logger.debug(message = s"${response.status}: ${response.body}")
        s"${response.status}: ${response.body}"
      }
    }.recover {
      case e: Exception => e.getMessage
    }
  }
}
