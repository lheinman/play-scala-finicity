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

  def patchCustomer: Future[String]  = {
    val request: WSRequest = ws
      .url(url = "https://api.finicity.com/aggregation/v2/partners/aggregation/v1/customers/testing")
      .addHttpHeaders("Finicity-App-Key" -> sys.env("FINICITY_APP_KEY"))
      .addHttpHeaders("Finicity-App-Token" -> Await.result(tRepo.last(), Duration.Inf).get.token)
      .addHttpHeaders("Accept" -> "application/json")

    val borrower = Await.result(pRepo.last(), Duration.Inf).get
    val data = Json.obj(
      fields = "username" -> borrower.username,
      "firstName" -> borrower.firstName,
      "lastName" -> borrower.lastName)
    request.post(data).map { response =>
      val body: JsValue = Json.parse(response.body)
      Logger.debug(body.toString())
      pRepo.patchCustomer(borrower.id, (body \ "id").as[String], (body \ "createdDate").as[Long])
      (body \ "username").as[String]
    }
  }

  def patchCustomers: Future[String]  = {
    val finicityRequest: WSRequest = ws
      .url(url = "https://api.finicity.com/aggregation/v1/customers")
      .addHttpHeaders("Finicity-App-Key" -> sys.env("FINICITY_APP_KEY"))
      .addHttpHeaders("Finicity-App-Token" -> Await.result(tService.getToken, Duration.Inf))
      .addHttpHeaders("Accept" -> "application/json")

    finicityRequest.get().map {
      response => {
        var out = "users: "
        Json.parse(response.body).validate[Customers].map{
          case customers => {
            customers.customers.foreach(customer => {
              out += (if (!Await.result(pRepo.isBorrowerByCustomer(customer.id), Duration.Inf)) {
                Await.result(pRepo.addNewCustomer(customer), Duration.Inf)
                customer.id + ","
              })
            })
          }
        }
        out
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