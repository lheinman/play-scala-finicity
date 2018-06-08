package controllers

import javax.inject.Inject
import play.api.mvc.{Action, AnyContent, MessagesAbstractController, MessagesControllerComponents}
import services.CustomerService

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class CustomerController @Inject()(
                                    customerService: CustomerService,
                                    cc: MessagesControllerComponents)(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

  def postCustomer: Action[AnyContent] = Action.async { implicit request =>
    Future{
      Ok(Await.result(customerService.postCustomer, Duration.Inf))
    }
  }

  def patchCustomers: Action[AnyContent] = Action.async { implicit request =>
    Future{
      Ok(Await.result(customerService.patchCustomers, Duration.Inf))
    }
  }
}
