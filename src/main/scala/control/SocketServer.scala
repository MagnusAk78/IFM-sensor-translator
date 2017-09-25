package control

import akka.actor.{ Actor, ActorRef, Props }
import akka.io.{ IO, Tcp }

class SocketServer extends Actor {
  
  import Tcp._
  import context.system // implicitly used by IO(Tcp)
   
  
 
  def receive = {
    case b @ Bound(localAddress) ⇒
      // do some logging or setup ...
 
    case CommandFailed(_: Bind) ⇒ context stop self
 
    case c @ Connected(remote, local) ⇒
      val handler = context.actorOf(Props[SensorDataHandler])
      val connection = sender
      connection ! Register(handler)
  }
 
}