package control

import akka.actor.{ Actor, ActorRef, Props }
import akka.io.{ IO, Tcp }
import akka.util.ByteString
import model.Sensor
import model.SensorInfo
import scala.collection.mutable.ListMap
import scala.collection.mutable.ListBuffer

case class AskSensorInfoMessage(sensorId: Long)
case class AskLatestSensorMessage(sensorId: Long)
case class AskTimestampSensorMessage(sensorId: Long, timestamp: Long)
case object AskSensorListMessage

class SensorDataHandler extends Actor with akka.actor.ActorLogging {
  
  val MAX_NUMBER_OF_VALUES = 1000
  
  val sensorMap = ListMap[Long, ListBuffer[Sensor]]()
  
  val hexPrefix = "0x"
  
  def hexToLong(s: String): Long = {
    s.toLowerCase.toList.map("0123456789abcdef".indexOf(_)).reduceLeft(_ * 16 + _)
}
    
  def receive = {
    case RawSensorData(value1, value2, value3, value4, value5, value6, value7) => {
      
      val keyValue = hexToLong(value4)
      val list = sensorMap.get(keyValue) match {
        case Some(sensorList) => sensorList 
        case None => {
          val newList = ListBuffer[Sensor]()
          sensorMap.put(keyValue, newList)
          newList
        }
      }
      
      val timestamp = System.currentTimeMillis
      list.insert(0, Sensor(System.currentTimeMillis, 
              value1.toLong, 
              value2.toLong,
              value3.toLong,
              hexToLong(value4),
              value5,
              value6,
              hexToLong(value7)))
      if(list.size > MAX_NUMBER_OF_VALUES) {
        list.dropRight(1)
      }
      
    }
    case AskSensorInfoMessage(sensorId) => {
      val list = sensorMap.get(sensorId).getOrElse(ListBuffer[Sensor]())
      sender ! SensorInfo(
          sensorId, 
          list.length, 
          list.headOption.map { sensor:Sensor => sensor.timestamp }.getOrElse(0))
    }
    case AskLatestSensorMessage(sensorId) => {
      sender ! sensorMap.get(sensorId).getOrElse(ListBuffer[Sensor]()).headOption
    }
    case AskTimestampSensorMessage(sensorId, timestamp) => {
      val fullList = sensorMap.get(sensorId).getOrElse(ListBuffer[Sensor]())
      sender ! fullList.takeWhile { sensor: Sensor => sensor.timestamp > timestamp}.toList
    }
    case AskSensorListMessage => {
      sender ! sensorMap.keys.toList
    }
  }
}