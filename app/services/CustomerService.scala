package services

import javax.inject.Inject
import models.{Customer, PersonRepository, TokenRepository}
import play.api.Logger
import play.api.libs.json._
import play.api.libs.ws.{WSClient, WSRequest}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration

class CustomerService @Inject() (
                                  pRepo: PersonRepository,
                                  tRepo: TokenRepository,
                                  tService: TokenService,
                                  ws: WSClient)(implicit ec: ExecutionContext) {

  def postCustomer: Future[String] = {
    val request: WSRequest = ws
      .url(url = "https://api.finicity.com/aggregation/v1/customers/testing")
      .addHttpHeaders(hdrs = "Finicity-App-Key" -> sys.env("FINICITY_APP_KEY"))
      .addHttpHeaders(hdrs = "Finicity-App-Token" -> Await.result(tService.getToken, Duration.Inf))
      .addHttpHeaders(hdrs = "Accept" -> "application/json")

    val borrower = Await.result(pRepo.firstNoCustomer(), Duration.Inf).get
    val data = Json.obj(fields =
      "username" -> borrower.username,
      "firstName" -> borrower.firstName,
      "lastName" -> borrower.lastName)
    request.post(data).map { response =>
      if (response.status == 201) {
        Json.parse(response.body).validate[Customer].map {
          case customer => {
            pRepo.patchCustomer(borrower.username, customer.id, customer.createdDate.toLong)
            customer.username
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

  def patchCustomers: Future[String] = {
    val finicityRequest: WSRequest = ws
      .url(url = "https://api.finicity.com/aggregation/v1/customers")
      .addHttpHeaders(hdrs = "Finicity-App-Key" -> sys.env("FINICITY_APP_KEY"))
      .addHttpHeaders(hdrs = "Finicity-App-Token" -> Await.result(tService.getToken, Duration.Inf))
      .addHttpHeaders(hdrs = "Accept" -> "application/json")

    finicityRequest.get().map {
      response => {
        if (response.status == 200) {
          Json.parse(response.body).validate[Customers].map {
            case customers => {
              customers.customers.foreach(customer => {
                if (!Await.result(pRepo.isBorrowerByUsername(customer.username), Duration.Inf)) {
                  Await.result(pRepo.addNewCustomer(customer), Duration.Inf)
                } else {
                  Await.result(pRepo.patchCustomer(customer.username, customer.id, customer.createdDate.toLong), Duration.Inf)
                }
              }).toString
            }
          }.getOrElse("Json.parse error")
        } else {
          Logger.debug(message = s"${response.status}: ${response.body}")
          s"${response.status}: ${response.body}"
        }
      }
    }
  }
}

case class Customers
(
  found: Int,
  displaying: Int,
  moreAvailable: Boolean,
  customers: List[Customer])

object Customers {
  implicit val readsCustomers: Reads[Customers] = new Reads[Customers] {
    def reads(json: JsValue): JsResult[Customers] = {
      for {
        found <- (json \ "found").validate[Int]
        displaying <- (json \ "displaying").validate[Int]
        moreAvailable <- (json \ "moreAvailable").validate[Boolean]
        customers <- (json \ "customers").validate[List[Customer]]
      } yield Customers(found, displaying, moreAvailable, customers)
    }
  }
}