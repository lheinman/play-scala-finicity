package services

import javax.inject.Inject

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import play.api.libs.ws._
import play.api.libs.json._
import play.api.libs.ws.JsonBodyWritables._
import models.TokenRepository
import play.api.Logger

class TokenService @Inject() (
                               tRepo: TokenRepository,
                               ws: WSClient)(implicit ec: ExecutionContext) {

  def getToken: Future[String] = {
    val token = Await.result(tRepo.last(), Duration.Inf)
    if (token == None || System.currentTimeMillis()/1000 - token.get.time >= (7200 - 100)) {
      val request: WSRequest = ws
        .url(url = "https://api.finicity.com/aggregation/v2/partners/authentication")
        .addHttpHeaders(hdrs = "Finicity-App-Key" -> sys.env("FINICITY_APP_KEY"))
        .addHttpHeaders(hdrs = "Accept" -> "application/json")

      val data = Json.obj(fields =
        "partnerId" -> sys.env("FINICITY_PARTNER_ID"),
        "partnerSecret" -> sys.env("FINICITY_PARTNER_SECRET"))
      request.post(data).map { response =>
        if (response.status == 200) {
          val newToken = (Json.parse(response.body) \ "token").as[String]
          tRepo.upsert(newToken)
          newToken
        } else {
          Logger.debug(message = s"${response.status}: ${response.body}")
          s"${response.status}: ${response.body}"
        }
      }
    } else {
      Future {token.get.token}
    }
  }
}