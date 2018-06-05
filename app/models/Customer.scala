package models

import play.api.libs.json.{JsResult, JsValue, Json, Reads}

case class Customer
(
  id: String,
  username: String,
  firstName: String,
  lastName: String,
  `type`: String,
  createdDate: String)

object Customer {
  implicit val customerFormat = Json.format[Customer]
}
