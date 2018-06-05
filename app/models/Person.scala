package models

import play.api.libs.json._

case class Person
(
  username: String,
  firstName: String,
  lastName: String,
  month: Option[String],
  dayOfMonth: Option[String],
  year: Option[String],
  emailAddress: Option[String],
  address: Option[String],
  city: Option[String],
  state: Option[String],
  zip: Option[String],
  phone: Option[String],
  ssn: Option[String],
  customer: Option[String],
  customerTime: Option[Long],
  consumer: Option[String],
  consumerTime: Option[Long],
  report: Option[String],
  reportTime: Option[Long],
  id: Long = 0L)

object Person {  
  implicit val personFormat = Json.format[Person]
}
