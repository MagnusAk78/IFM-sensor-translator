package control

import akka.actor.{ Actor, ActorRef, Props }
import akka.io.{ IO, Tcp }
import akka.util.ByteString
import model._
import scala.collection.mutable.ListMap
import scala.collection.mutable.ListBuffer
import model.RawSensorData
import rest.Settings

// SensorDataHandler actor messages
case object AskSensorsInfoMessage
case object AskSensorsListMessage
case object AskSensorsStatusMessage

case class AskSensorInfoMessage(sensorId: String)
case class AskLastProcessDataMessage(sensorId: String)
case class AskProcessDataAfterTimestampMessage(sensorId: String, timestamp: Long)
case class AskSensorStatusMessage(sensorId: String)

// SensorDataHandler
class SensorDataHandler extends Actor with akka.actor.ActorLogging {

  // Products
  val O5D100 = "O5D100" //Distance laser sensor
  val TP3237 = "TP3237" //I/O-link temperature converter
  val SA5000 = "SA5000" //Flow and temperature sensor
  val TN2405 = "TN2405" //Temperature sensor

  // Information
  
  //Get settings
  val settings = Settings(context.system)  

  val MAX_NUMBER_OF_VALUES = settings.MaxNumberOfProcessData

  val sensorInfoMap = ListMap[String, SensorInfo]()
  val processDataMap = ListMap[String, ListBuffer[ProcessData]]()

  def hexToInt(s: String): Int = {
    s.toLowerCase.toList.map("0123456789abcdef".indexOf(_)).reduceLeft(_ * 16 + _)
  }

  def receive = {
    case RawSensorData(sourceId, eventTypeHS, portNrHS, vendorIdHS, deviceIdHS, productId, deviceSerialNumber, 
        processDataHS, timestamp) => {

      // Source ID + the port number together is the sensor identifier
      val keyValue = sourceId + hexToInt(portNrHS)

      // Make sure there is a sensorInfo defined for this specific sensor
      if (sensorInfoMap.isDefinedAt(keyValue) == false) {
        sensorInfoMap.put(keyValue, SensorInfo(keyValue,
          hexToInt(vendorIdHS),
          hexToInt(deviceIdHS),
          productId,
          deviceSerialNumber,
          productId match {
            case O5D100 => "Integer value: distance in cm"
            case TP3237 => "Float value: temperature in Celcius"
            case SA5000 => "Integer value: flow in %. Float value: temperature in Celcius"
            case TN2405 => "Float value: temperature in Celcius"
          },
          productId match {
            case O5D100 => Some("cm")
            case TP3237 => None
            case SA5000 => Some("%")
            case TN2405 => None
          },
          productId match {
            case O5D100 => None
            case TP3237 => Some("Celcius")
            case SA5000 => Some("Celcius")
            case TN2405 => Some("Celcius")
          },
          productId match {
            case O5D100 => Some(List("Value below trigger distance"))
            case TP3237 => None
            case SA5000 => Some(List("Value below trigger temperature", "Value above trigger temperature"))
            case TN2405 => Some(List("Value below trigger temperature", "Value above trigger temperature"))
          }))
      }

      // Only react to new process data, event type = 0
      hexToInt(eventTypeHS) match {
        case 0 => {
          //Process data
          val list = processDataMap.get(keyValue) match {
            case Some(sensorList) => sensorList
            case None => {
              val newList = ListBuffer[ProcessData]()
              processDataMap.put(keyValue, newList)
              newList
            }
          }

          productId match {
            case O5D100 => {
              val tuple = processDataHS.splitAt(processDataHS.length - 1)

              list.insert(0, ProcessData(
                keyValue,
                timestamp,
                Some(hexToInt(tuple._1)),
                None,
                Some(List(hexToInt(tuple._2) == 1))))
            }
            case TP3237 => {
              val tempValue: Double = hexToInt(processDataHS) * 0.1

              list.insert(0, ProcessData(
                keyValue,
                timestamp,
                None,
                Some(tempValue),
                None))
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
                Some(flowValue),
                Some(tempValue),
                Some(List(out, out2))))
            }
            case TN2405 => {
              val restValue = hexToInt(processDataHS)
              val tempValue: Double = (restValue >> 2) * 0.1
              val out = (restValue & 2) > 0
              val out2 = (restValue & 1) > 0

              list.insert(0, ProcessData(
                keyValue,
                timestamp,
                None,
                Some(tempValue),
                Some(List(out, out2))))
            }
          }

          while (list.size > MAX_NUMBER_OF_VALUES) {
            list.remove(list.size-1)
          }
        }
        case _ => {
          //Some other event, 3 => Sensor disconnected
        }
      }
    }
    case AskSensorsInfoMessage => {
      sender ! sensorInfoMap.values.toList
    }
    case AskSensorsListMessage => {
      sender ! sensorInfoMap.keys.toList
    }
    case AskSensorsStatusMessage => {
      sender ! processDataMap.toList.map((keyProcessDataListTuple: (String, ListBuffer[ProcessData])) =>
        SensorStatus(keyProcessDataListTuple._1, keyProcessDataListTuple._2.size, MAX_NUMBER_OF_VALUES,
          keyProcessDataListTuple._2.head.timestamp)).toList
    }
    case AskSensorInfoMessage(sensorId) => {
      sender ! sensorInfoMap.get(sensorId)
    }
    case AskLastProcessDataMessage(sensorId) => {
      sender ! processDataMap.get(sensorId).getOrElse(ListBuffer[ProcessData]()).headOption
    }
    case AskProcessDataAfterTimestampMessage(sensorId, timestamp) => {
      val fullList = processDataMap.get(sensorId).getOrElse(ListBuffer[ProcessData]())
      sender ! fullList.takeWhile { pd: ProcessData => pd.timestamp > timestamp }.toList
    }
    case AskSensorStatusMessage(sensorId) => {
      sender ! processDataMap.get(sensorId).map((processDataList: ListBuffer[ProcessData]) => SensorStatus(sensorId,
        processDataList.size, MAX_NUMBER_OF_VALUES, processDataList.head.timestamp))
    }
  }
}