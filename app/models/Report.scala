package models

import play.api.libs.json.Json

case class Report
(
  id: String,
  consumerId: String,
  consumerSsn: String,
  requesterName: String,
  `type`: String,
  status: String,
  createdDate: Long,
  customerId: Long)

object Report {
  implicit val reportFormat = Json.format[Report]
}
