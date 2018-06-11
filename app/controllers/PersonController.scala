package controllers

import javax.inject._
import models._
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import play.api.Logger
import play.api.libs.json.Json

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class PersonController @Inject()(
                                  pRepo: PersonRepository,
                                  cc: MessagesControllerComponents)(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

  /**
   * The mapping for the person form.
   */
  val personForm: Form[CreatePersonForm] = Form {
    mapping(
      a1 = "username" -> nonEmptyText,
      a2 = "firstName" -> nonEmptyText,
      a3 = "lastName" -> nonEmptyText,
      a4 = "month" -> nonEmptyText,
      a5 = "dayOfMonth" -> nonEmptyText,
      a6 = "year" -> nonEmptyText,
      a7 = "emailAddress" -> nonEmptyText,
      a8 = "address" -> nonEmptyText,
      a9 = "city" -> nonEmptyText,
      a10 = "state" -> nonEmptyText,
      a11 = "zip" -> nonEmptyText,
      a12 = "phone" -> nonEmptyText,
      a13 = "ssn" -> nonEmptyText
    )(CreatePersonForm.apply)(CreatePersonForm.unapply)
  }

  /**
   * The index action.
   */
  def index = Action { implicit request =>
    Ok(views.html.index(personForm))
  }

  /**
   * The add person action.
   *
   * This is asynchronous, since we're invoking the asynchronous methods on PersonRepository.
   */
  def addPerson: Action[AnyContent] = Action.async { implicit request =>
    // Bind the form first, then fold the result, passing a function to handle errors, and a function to handle succes.
    personForm.bindFromRequest.fold(
      // The error function. We return the index page with the error form, which will render the errors.
      // We also wrap the result in a successful future, since this action is synchronous, but we're required to return
      // a future because the person creation function returns a future.
      errorForm => {
        Future.successful(Ok(views.html.index(errorForm)))
      },
      // There were no errors in the from, so create the person.
      person => {
        pRepo.create(
          person.username,
          person.firstName,
          person.lastName,
          Some(person.month),
          Some(person.dayOfMonth),
          Some(person.year),
          Some(person.emailAddress),
          Some(person.address),
          Some(person.city),
          Some(person.state),
          Some(person.zip),
          Some(person.phone),
          Some(person.ssn),
          customer = null,
          customerTime = null,
          consumer = null,
          consumerTime = null,
          report = null,
          reportTime = null).map { _ =>
          // If successful, we simply redirect to the index page.
          Redirect(routes.PersonController.index).flashing(values = "success" -> "user.created")
        }
      }
    )
  }

  /**
   * A REST endpoint that gets all the borrowers as JSON.
   */
  def getPersons: Action[AnyContent] = Action.async { implicit request =>
    pRepo.list().map { people =>
      Logger.debug(people.toString())
      Ok(Json.toJson(people))
    }
  }

  /**
    * A REST endpoint that gets the last borrower as JSON.
    */
  def getLastPerson: Action[AnyContent] = Action.async { implicit request =>
    pRepo.last().map { person => Ok(Json.toJson(person)) }
  }

  def patchPerson: Action[AnyContent] = Action.async { implicit request =>
    pRepo.patchPerson().map { person => Ok(Json.toJson(person)) }
  }
}

/**
 * The create person form.
 *
 * Generally for forms, you should define separate objects to your models, since forms very often need to present data
 * in a different way to your models.  In this case, it doesn't make sense to have an id parameter in the form, since
 * that is generated once it's created.
 */
case class CreatePersonForm(
    username: String, 
    firstName: String, 
    lastName: String, 
    month: String, 
    dayOfMonth: String, 
    year: String, 
    emailAddress: String, 
    address: String, 
    city: String, 
    state: String, 
    zip: String, 
    phone: String, 
    ssn: String)
