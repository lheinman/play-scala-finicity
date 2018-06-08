package models

import play.api.libs.json.Json

case class NewConsumer
(
  id: String,
  createdDate: Long)

object NewConsumer {
  implicit val newConsumerFormat = Json.format[NewConsumer]
}
