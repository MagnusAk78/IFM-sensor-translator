package model

import spray.json._
import spray.json.DefaultJsonProtocol._

case class Sensor(timestamp: Long, 
    value1: Long, 
    value2: Long,
    value3: Long,
    value4: Long,
    value5: String,
    value6: String,
    value7: Long
    )
    
case class SensorInfo( 
    sensorId: Long, 
    values: Int,
    latestTimestamp: Long
    )    

object SensorJsonSupport extends DefaultJsonProtocol {
   implicit val SensorFormats = jsonFormat8(Sensor)
   implicit val SensorInfoFormats = jsonFormat3(SensorInfo)
}
