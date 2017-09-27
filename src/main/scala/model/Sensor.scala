package model

import spray.json._
import spray.json.DefaultJsonProtocol._

trait DataType {
  val out: Option[Boolean]
  val out2: Option[Boolean]
}

case class Temperature(
    info: String = "Float temperature in Celcius",
    value: Float,
    override val out: Option[Boolean] = None,
    override val out2: Option[Boolean] = None
    ) extends DataType
    
case class Distance(
    info: String = "Integer distance in cm",
    value: Int,
    override val out: Option[Boolean] = None,
    override val out2: Option[Boolean] = None
    ) extends DataType
    
case class Flow(
    info: String = "Integer flow in %",
    value: Int,
    override val out: Option[Boolean] = None,
    override val out2: Option[Boolean] = None
    ) extends DataType    

case class ProcessData(
    // Source ID and connection port, key identifier of sensor
    sourceAndPort: String,
    
    // Timestamp from server when data was received (not from sensor)
    timestamp: Long, 
     
    // Sensor vendor ID
    vendorId: Int,
    
    // Sensor device ID
    deviceId: Int,
    
    // Sensor product ID
    productId: String,
    
    // Sensor device serial nr (Unique but Not always present)
    deviceSerialNr: String,
    
    info: String,
    intValue: Option[Int],
    floatValue: Option[Double],
    out: Option[Boolean],
    out2: Option[Boolean]
    )

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
    sensorId: String, 
    values: Int,
    latestTimestamp: Long
    )    

object SensorJsonSupport extends DefaultJsonProtocol {
   implicit val SensorFormats = jsonFormat8(Sensor)
   implicit val SensorInfoFormats = jsonFormat3(SensorInfo)
   implicit val ProcessDataFormat = jsonFormat11(ProcessData)
}
