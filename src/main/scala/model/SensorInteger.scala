package model

import spray.json._
import spray.json.DefaultJsonProtocol._

case class SensorInteger(name: String, value: Int, timestamp: Long)

object SensorIntegerJsonSupport extends DefaultJsonProtocol {
   implicit val SensorIntegerFormats = jsonFormat3(SensorInteger)
}
