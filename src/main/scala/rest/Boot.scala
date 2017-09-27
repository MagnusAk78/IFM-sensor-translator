package rest

import akka.actor.{ ActorSystem, Props }
import akka.io.{ IO, Tcp }
import spray.can.Http
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import java.net.InetSocketAddress

import control.SocketServer
import control.SensorDataHandler

object Boot extends App {

  import Tcp._

  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("on-spray-can")

  //Create and start the SensorDataHandler
  val dataHandler = system.actorOf(Props[SensorDataHandler], "ifm-sensor-data-handler")

  // create and start our service actor
  val service = system.actorOf(Props(new MyServiceActor(dataHandler)), "ifm-sensor-service")

  // TCP Server actor
  val sensorReader = system.actorOf(Props(new SocketServer(dataHandler)), "sensor-reader")

  implicit val timeout = Timeout(10.seconds)

  // start a new HTTP server on port 8080 with our service actor as the handler
  IO(Http) ? Http.Bind(service, interface = "", port = 8080)

  // start a new TCP server on port 3010 with our sensorReader actor as the handler
  IO(Tcp) ! Bind(sensorReader, new InetSocketAddress("localhost", 34100))
}
