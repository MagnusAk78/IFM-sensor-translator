package control

import akka.actor.{ Actor, ActorRef, Props }
import akka.io.{ IO, Tcp }
import akka.util.ByteString

class SensorDataHandler extends Actor with akka.actor.ActorLogging {

  import Tcp._
  
  val start = ByteString("start")
  val end = ByteString("end")
  val dash = ByteString("-")
  val separator = ByteString("<>")
  
  var currentData = ByteString.empty
  
  def handleSensorData(sensorData: ByteString) {
    val dataTuple = sensorData.splitAt(sensorData.indexOfSlice(dash))
    log.info("sensor: " + dataTuple._1.utf8String)
    log.info("value: " + dataTuple._2.drop(dash.length).utf8String)
  }
    
  def receive = {
    case Received(newData) => {
      log.info("Receiving data: " + newData.utf8String)
      currentData = currentData ++ newData
      while(currentData.containsSlice(start) && currentData.containsSlice(end)) {

        //Drop start
        currentData = currentData.drop(currentData.indexOfSlice(start) + start.length)
        
        //Loop while multiple sensor values
        while(currentData.containsSlice(separator)) {
          val separatorIndex = currentData.indexOfSlice(separator)
          val dataTuple = currentData.splitAt(separatorIndex)
          log.info("separatorIndex:" + separatorIndex)
          log.info("dataTuple._1:" + dataTuple._1.utf8String)
          log.info("dataTuple._2:" + dataTuple._2.utf8String)
          handleSensorData(dataTuple._1)
          currentData = dataTuple._2.drop(separator.length)
        }
        handleSensorData(currentData.splitAt(currentData.indexOfSlice(end))._1)
        currentData = currentData.drop(currentData.indexOfSlice(end) + end.length)
      }
    }
    case PeerClosed     => context stop self
  }
}