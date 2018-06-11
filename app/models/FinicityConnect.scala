package models

import play.api.libs.json.Json

case class FinicityConnect(link: String)

object FinicityConnect {
  implicit val newFinicityConnectFormat = Json.format[FinicityConnect]
}
