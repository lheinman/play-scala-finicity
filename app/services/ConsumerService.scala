package services

import javax.inject.Inject
import models.{Consumer, PersonRepository, TokenRepository}
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

  def patchConsumerByCustomer(customer: String): Future[Option[Consumer]]  = {
    val finicityRequest: WSRequest = ws
      .url(url = s"https://api.finicity.com/decisioning/v1/customers/$customer/consumer")
      .addHttpHeaders(hdrs = "Finicity-App-Key" -> sys.env("FINICITY_APP_KEY"))
      .addHttpHeaders(hdrs = "Finicity-App-Token" -> Await.result(tService.getToken, Duration.Inf))
      .addHttpHeaders(hdrs = "Accept" -> "application/json")

    finicityRequest.get().map {
      response => {
        var out: Option[Consumer] = None
        Json.parse(response.body).validate[Consumer].map{
          case consumer => {
            if (Await.result(pRepo.isBorrowerByCustomer(customer), Duration.Inf)) {
              Await.result(pRepo.patchConsumer(customer, consumer.id, consumer.createdDate), Duration.Inf)
              out = Some(consumer)
            }
          }
        }
        out
      }
    }
  }

  def patchConsumers: Future[String]  = {
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
              val consumer = Await.result(patchConsumerByCustomer(customer.id), Duration.Inf)
              out += (if (consumer != None && !Await.result(pRepo.isBorrowerByConsumer(consumer.get.id), Duration.Inf)) {
                Await.result(pRepo.patchConsumer(customer.id, consumer.get.id, consumer.get.createdDate), Duration.Inf)
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
