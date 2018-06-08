package controllers

import javax.inject.Inject
import models.PersonRepository
import play.api.mvc.{Action, AnyContent, MessagesAbstractController, MessagesControllerComponents}
import services.ConsumerService

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration

class ConsumerController @Inject()(
                                    pRepo: PersonRepository,
                                    consumerService: ConsumerService,
                                    cc: MessagesControllerComponents)(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

  def postConsumer: Action[AnyContent] = Action.async { implicit request =>
    Future{
      val customer = Await.result(pRepo.firstNoConsumer(), Duration.Inf).get.customer
      if (customer != None) Ok(Await.result(consumerService.postConsumer(customer.get), Duration.Inf))
      else Ok("No customers with unintialized consumers")
    }
  }

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
