package model

import spray.json._
import spray.json.DefaultJsonProtocol._

object IfmJsonSupport extends DefaultJsonProtocol {
  implicit val ProcessDataFormat = jsonFormat5(ProcessData)
  implicit val SensorInfoFormat = jsonFormat9(SensorInfo)
  implicit val SensorStatusFormat = jsonFormat4(SensorStatus)
  implicit val ErrorMessageFormat = jsonFormat1(ErrorMessage)
}