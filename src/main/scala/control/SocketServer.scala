package control

import akka.actor.{ Actor, ActorRef, Props }
import akka.io.{ IO, Tcp }

class SocketServer(sensorDataHandler: ActorRef) extends Actor with akka.actor.ActorLogging {
  
  import Tcp._
  import context.system // implicitly used by IO(Tcp)
   
  def receive = {
    case b @ Bound(localAddress) ⇒
      log.info("SocketServer - Bound, " + localAddress.toString)
      // do some logging or setup ...
 
    case CommandFailed(_: Bind) ⇒ {
      log.info("SocketServer - CommandFailed")
      context stop self
    }
 
    case c @ Connected(remote, local) ⇒
      log.info("SocketServer - Connected, remote: " + remote.toString)
      val sensorDataReader = context.actorOf(Props(new SensorDataReader(sensorDataHandler)))
      val connection = sender
      connection ! Register(sensorDataReader)
  }
}