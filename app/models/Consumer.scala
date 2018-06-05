package models

import play.api.libs.json.Json

case class Birthday
(
  year: Int,
  month: Int,
  dayOfMonth: Int)

object Birthday {
  implicit val birthdayFormat = Json.format[Birthday]
}

case class Consumer
(
  id: String,
  firstName: String,
  lastName: String,
  address: String,
  city: String,
  state: String,
  zip: String,
  phone: String,
  ssn: String,
  birthday: Birthday,
  email: String,
  createdDate: Long)

object Consumer {
  implicit val consumerFormat = Json.format[Consumer]
}
