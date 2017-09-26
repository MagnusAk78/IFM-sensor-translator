package control

import akka.actor.{ Actor, ActorRef, Props }
import akka.io.{ IO, Tcp }
import akka.util.ByteString

class SensorDataReader(sensorDataHandler: ActorRef) extends Actor with akka.actor.ActorLogging {

  import Tcp._

  val semiColon = ByteString(";")
  val AY1020 = ByteString("AY1020")
  val newLine = ByteString("\n")

  def receive = {
    case Received(data) => {
      log.info("Receiving data: " + data.utf8String)

      val lines = scala.collection.mutable.ListBuffer[ByteString]()

      var remainingData = data
      while (remainingData.containsSlice(newLine)) {
        val tuple = remainingData.splitAt(remainingData.indexOfSlice(newLine))
        log.info("Adding line: " + tuple._1.utf8String)
        lines.append(tuple._1)
        remainingData = tuple._2.drop(newLine.length)
      }
      if (remainingData.length > 10) {
        log.info("Adding line: " + remainingData.utf8String)
        lines.append(remainingData)
      }

      for (line <- lines) {
        handleLine(line)
      }
    }
    case PeerClosed => context stop self
  }

  def handleLine(line: ByteString) {
    //Drop unimportant start
    val afterAY1020 = line.drop(line.indexOfSlice(AY1020) + AY1020.length + semiColon.length)

    val tuple1 = afterAY1020.splitAt(afterAY1020.indexOfSlice(semiColon))
    val value1 = tuple1._1

    val remaining1 = tuple1._2.drop(semiColon.length)
    val tuple2 = remaining1.splitAt(remaining1.indexOfSlice(semiColon))
    val value2 = tuple2._1

    val remaining2 = tuple2._2.drop(semiColon.length)
    val tuple3 = remaining2.splitAt(remaining2.indexOfSlice(semiColon))
    val value3 = tuple3._1

    val remaining3 = tuple3._2.drop(semiColon.length)
    val tuple4 = remaining3.splitAt(remaining3.indexOfSlice(semiColon))
    val value4 = tuple4._1

    val remaining4 = tuple4._2.drop(semiColon.length)
    val tuple5 = remaining4.splitAt(remaining4.indexOfSlice(semiColon))
    val value5 = tuple5._1

    val remaining5 = tuple5._2.drop(semiColon.length)
    val tuple6 = remaining5.splitAt(remaining5.indexOfSlice(semiColon))
    val value6 = tuple6._1

    val value7 = tuple6._2.drop(semiColon.length)

    log.info("value1: " + value1.utf8String)
    log.info("value2: " + value2.utf8String)
    log.info("value3: " + value3.utf8String)
    log.info("value4: " + value4.utf8String)
    log.info("value5: " + value5.utf8String)
    log.info("value6: " + value6.utf8String)
    log.info("value7: " + value7.utf8String)

    sensorDataHandler ! RawSensorData(value1.utf8String, value2.utf8String, value3.utf8String,
      value4.utf8String, value5.utf8String, value6.utf8String, value7.utf8String)
  }
}