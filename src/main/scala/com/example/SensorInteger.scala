package com.example

import spray.json._
import DefaultJsonProtocol._

case class SensorInteger(name: String, value: Int, timestamp: Int)

object MyJsonSupport extends DefaultJsonProtocol {
   implicit val PortofolioFormats = jsonFormat3(SensorInteger)
}
