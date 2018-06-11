package controllers

import javax.inject.Inject
import models.PersonRepository
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesAbstractController, MessagesControllerComponents}
import services.FinicityService

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class FinicityController @Inject()(
                                    pRepo: PersonRepository,
                                    finicityService: FinicityService,
                                    cc: MessagesControllerComponents)(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

  /**
    * The mapping for the finicity form.
    */
  val finicityForm: Form[GetFinicityConnectForm] = Form {
    mapping(
      a1 = "id" -> number.verifying(min(0), max(200))
    )(GetFinicityConnectForm.apply)(GetFinicityConnectForm.unapply)
  }

  /**
    * The index action.
    */
  def index = Action { implicit request =>
    Ok(views.html.finicity(finicityForm))
  }

  def postFinicityConnect: Action[AnyContent] = Action.async { implicit request =>
    Future{
      val borrower = Await.result(pRepo.firstNoReport(), Duration.Inf).get
      val link = Await.result(finicityService.postFinicityConnect(borrower), Duration.Inf)
      if (borrower != None) Ok(views.html.connect(link))
      else Ok("No consumers with unintialized reports")
    }
  }

}

case class GetFinicityConnectForm(id: Int)
