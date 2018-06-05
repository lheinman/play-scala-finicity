package controllers

import javax.inject.Inject
import play.api.mvc.{Action, AnyContent, MessagesAbstractController, MessagesControllerComponents}
import services.ConsumerService

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration

class ConsumerController @Inject()(
                                    consumerService: ConsumerService,
                                    cc: MessagesControllerComponents)(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

  def patchConsumerByCustomer(customer: String): Action[AnyContent] = Action.async { implicit request =>
    Future{
      val consumer = Await.result(consumerService.patchConsumerByCustomer(customer), Duration.Inf)
      if (consumer != None) Ok(consumer.get.id)
      else Ok("No such consumer")
    }
  }

  def patchConsumers: Action[AnyContent] = Action.async { implicit request =>
    Future{
      Ok(Await.result(consumerService.patchConsumers, Duration.Inf))
    }
  }
}
