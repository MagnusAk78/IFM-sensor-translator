package control

import akka.actor.{ Actor, ActorRef, Props }
import akka.io.{ IO, Tcp }
import akka.util.ByteString
import model.Sensor
import scala.collection.mutable.ListMap

case object AskLatestSensorMessage

class SensorDataHandler extends Actor with akka.actor.ActorLogging {
  
  val sensorMap = ListMap[Long, Sensor]()
  
  val hexPrefix = "0x"
  
  def hexStringToLong(hexString: String): Long = {
    Integer.decode(hexPrefix + hexString).toLong
  }
    
  def receive = {
    case RawSensorData(value1, value2, value3, value4, value5, value6, value7) => {
        
      log.info("SensorDataHandler, value1: " + value1)
      log.info("SensorDataHandler, value2: " + value2)
      log.info("SensorDataHandler, value3: " + value3)
      log.info("SensorDataHandler, value4: " + value4)
      log.info("SensorDataHandler, value5: " + value5)
      log.info("SensorDataHandler, value6: " + value6)
      log.info("SensorDataHandler, value7: " + value7)
      
      val timestamp = System.currentTimeMillis
      sensorMap.put(timestamp, Sensor(System.currentTimeMillis, 
          value1.toLong, 
          value2.toLong,
          value3.toLong,
          hexStringToLong(value4),
          value5,
          value6,
          hexStringToLong(value7)))
    }
    case AskLatestSensorMessage => {
      sender ! sensorMap.lastOption
    }
  }
}