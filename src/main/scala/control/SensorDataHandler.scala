package control

import akka.actor.{ Actor, ActorRef, Props }
import akka.io.{ IO, Tcp }
import akka.util.ByteString
import model._
import scala.collection.mutable.ListMap
import scala.collection.mutable.ListBuffer
import model.RawSensorData

case class AskSensorInfoMessage(sensorId: String)
case class AskLatestSensorMessage(sensorId: String)
case class AskTimestampSensorMessage(sensorId: String, timestamp: Long)
case object AskSensorListMessage

class SensorDataHandler extends Actor with akka.actor.ActorLogging {
  
  //Products
  val O5D100 = "O5D100" //Distance laser sensor
  val TP3237 = "TP3237" //I/O-link temperature converter
  val SA5000 = "SA5000" //Flow and temperature sensor
  val TN2405 = "TN2405" //Temperature sensor
  
  val MAX_NUMBER_OF_VALUES = 1000
  
  val sensorMap = ListMap[String, ListBuffer[ProcessData]]()
  
  def hexToLong(s: String): Long = {
    s.toLowerCase.toList.map("0123456789abcdef".indexOf(_)).reduceLeft(_ * 16 + _)
  }
  
  def hexToInt(s: String): Int = {
    s.toLowerCase.toList.map("0123456789abcdef".indexOf(_)).reduceLeft(_ * 16 + _)
}
    
  def receive = {
    case RawSensorData(sourceId, eventTypeHS, portNrHS, vendorIdHS, deviceIdHS, productId, deviceSerialNumber, processDataHS, timestamp) => {
      
      hexToInt(eventTypeHS) match {
        case 0 => {
          //Process data
          
      val keyValue = sourceId + hexToInt(portNrHS)
      val list = sensorMap.get(keyValue) match {
        case Some(sensorList) => sensorList 
        case None => {
          val newList = ListBuffer[ProcessData]()
          sensorMap.put(keyValue, newList)
          newList
        }
      }
      
      productId match {
        case O5D100 => {
          
          val tuple = processDataHS.splitAt(processDataHS.length - 1)
          val value = hexToInt(tuple._1)
          val out = hexToInt(tuple._2) == 1
          
          list.insert(0, ProcessData(
            keyValue,
            timestamp, 
            hexToInt(vendorIdHS),
            hexToInt(deviceIdHS),
            productId,
            deviceSerialNumber,
            "Integer distance in cm",
            Some(value), None, Some(out), None))
        }
        case TP3237 => {
          val tempValue: Double = hexToInt(processDataHS) * 0.1
          
          list.insert(0, ProcessData(
            keyValue,
            timestamp, 
            hexToInt(vendorIdHS),
            hexToInt(deviceIdHS),
            productId,
            deviceSerialNumber,
            "Float temperature in Celcius",
            None, Some(tempValue), None, None))          
        }
        case SA5000 => {
          val flowTuple = processDataHS.splitAt(4)
          val flowValue = hexToInt(flowTuple._1)
          val restValue = hexToInt(flowTuple._2)
          val tempValue: Double = (restValue >> 2) * 0.1
          val out = (restValue & 2) > 0
          val out2 = (restValue & 1) > 0
          
          list.insert(0, ProcessData(
            keyValue,
            timestamp, 
            hexToInt(vendorIdHS),
            hexToInt(deviceIdHS),
            productId,
            deviceSerialNumber,
            "Integer flow in % and Float temperature in Celcius, out: lower temp, out2: upper temp",
            Some(flowValue), Some(tempValue), Some(out), Some(out2)))          
        }
        case TN2405 => {
          val restValue = hexToInt(processDataHS)
          val tempValue: Double = (restValue >> 2) * 0.1
          val out = (restValue & 2) > 0
          val out2 = (restValue & 1) > 0
          
          list.insert(0, ProcessData(
            keyValue,
            timestamp, 
            hexToInt(vendorIdHS),
            hexToInt(deviceIdHS),
            productId,
            deviceSerialNumber,
            "Float temperature in Celcius, out: lower temp, out2: upper temp",
            None, Some(tempValue), Some(out), Some(out2)))          
        }
      }          
      
      if(list.size > MAX_NUMBER_OF_VALUES) {
              list.dropRight(1)
            }
      }
        case _ => {
          //Some other event, 3 => Sensor disconnected
        }
      }
    }
    case AskSensorInfoMessage(sensorId) => {
      val list = sensorMap.get(sensorId).getOrElse(ListBuffer[ProcessData]())
      sender ! SensorInfo(
          sensorId, 
          list.length, 
          list.headOption.map { pd:ProcessData => pd.timestamp }.getOrElse(0))
    }
    case AskLatestSensorMessage(sensorId) => {
      sender ! sensorMap.get(sensorId).getOrElse(ListBuffer[ProcessData]()).headOption
    }
    case AskTimestampSensorMessage(sensorId, timestamp) => {
      val fullList = sensorMap.get(sensorId).getOrElse(ListBuffer[ProcessData]())
      sender ! fullList.takeWhile { pd: ProcessData => pd.timestamp > timestamp}.toList
    }
    case AskSensorListMessage => {
      sender ! sensorMap.keys.toList
    }
  }
}