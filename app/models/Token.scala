package models

import play.api.libs.json._

case class Token(token: String, time: Long)

object Token { 
  implicit val tokenFormat = Json.format[Token]
}