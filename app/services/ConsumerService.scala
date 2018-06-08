package services

import javax.inject.Inject
import models.{Consumer, NewConsumer, PersonRepository, TokenRepository}
import play.api.Logger
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSRequest}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration

class ConsumerService @Inject() (
                                  pRepo: PersonRepository,
                                  tRepo: TokenRepository,
                                  tService: TokenService,
                                  customerService: CustomerService,
                                  ws: WSClient)(implicit ec: ExecutionContext) {

  def postConsumer(customer: String): Future[String] = {
    val request: WSRequest = ws
      .url(url = s"https://api.finicity.com/decisioning/v1/customers/$customer/consumer")
      .addHttpHeaders(hdrs = "Finicity-App-Key" -> sys.env("FINICITY_APP_KEY"))
      .addHttpHeaders(hdrs = "Finicity-App-Token" -> Await.result(tService.getToken, Duration.Inf))
      .addHttpHeaders(hdrs = "Accept" -> "application/json")

    val borrower = Await.result(pRepo.firstNoConsumer(), Duration.Inf).get
    val data = Json.obj(fields =
      "firstName" -> borrower.firstName,
      "lastName" -> borrower.lastName,
      "address" -> borrower.address,
      "city" -> borrower.city,
      "state" -> borrower.state,
      "zip" -> borrower.zip,
      "phone" -> borrower.phone,
      "ssn" -> borrower.ssn,
      "birthday" -> Json.obj(fields =
        "year" -> borrower.year,
        "month" -> borrower.month,
        "dayOfMonth" -> borrower.dayOfMonth),
      "email" -> borrower.emailAddress)
    request.post(data).map { response =>
      if (response.status == 201) {
        Json.parse(response.body).validate[NewConsumer].map {
          case newConsumer => {
            pRepo.patchConsumer(borrower.username, newConsumer.id, newConsumer.createdDate)
            newConsumer.id
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

  def patchConsumerByCustomer(customer: String): Future[Option[Consumer]]  = {
    val finicityRequest: WSRequest = ws
      .url(url = s"https://api.finicity.com/decisioning/v1/customers/$customer/consumer")
      .addHttpHeaders(hdrs = "Finicity-App-Key" -> sys.env("FINICITY_APP_KEY"))
      .addHttpHeaders(hdrs = "Finicity-App-Token" -> Await.result(tService.getToken, Duration.Inf))
      .addHttpHeaders(hdrs = "Accept" -> "application/json")

    finicityRequest.get().map {
      response => {
        if (response.status == 200) {
          Json.parse(response.body).validate[Consumer].map{
            case consumer => {
              if (Await.result(pRepo.isBorrowerByCustomer(customer), Duration.Inf)) {
                Await.result(pRepo.patchConsumer(customer, consumer.id, consumer.createdDate), Duration.Inf)
                Some(consumer)
              } else None
            }
          }.getOrElse(None)
        } else {
          Logger.debug(message = s"${response.status}: ${response.body}")
          None
        }
      }
    }
  }

  def patchConsumers: Future[String]  = {
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
              customers.customers.foreach(customer => {
                val consumer = Await.result(patchConsumerByCustomer(customer.id), Duration.Inf)
                if (consumer != None && !Await.result(pRepo.isBorrowerByConsumer(consumer.get.id), Duration.Inf)) {
                  Await.result(pRepo.patchConsumer(customer.id, consumer.get.id, consumer.get.createdDate), Duration.Inf)
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
