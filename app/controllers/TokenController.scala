package controllers

import javax.inject._
import play.api.libs.json.Json
import play.api.mvc._
import models._
import services.TokenService

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class TokenController @Inject()(
                                 tRepo: TokenRepository,
                                 tService: TokenService,
                                 cc: MessagesControllerComponents)(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

  def getTokens: Action[AnyContent] = Action.async { implicit request =>
    tRepo.list().map { tokens =>
      Ok(Json.toJson(tokens))
    }
  }
  
  def getToken: Action[AnyContent] = Action.async { implicit request =>
    Future {
      Ok(Await.result(tService.getToken, Duration.Inf))
    }
  }
}