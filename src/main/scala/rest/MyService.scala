package rest

import akka.actor.{ Actor, ActorRef }
import scala.concurrent.Await
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import spray.routing._
import spray.http._
import MediaTypes._

import model.SensorJsonSupport._
import model.Sensor
import spray.json._

import control.AskLatestSensorMessage

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class MyServiceActor(sensorDataHandler: ActorRef) extends Actor with HttpService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  def receive = runRoute(myRoute)
  
  implicit val timeout = Timeout(5 seconds)

  val myRoute = {
    import spray.httpx.SprayJsonSupport.sprayJsonMarshaller
    import spray.httpx.SprayJsonSupport.sprayJsonUnmarshaller
    
    pathPrefix("sensors") {
      path("info") {
        get {
          respondWithMediaType(`application/json`) { 
            complete {
              Sensor(System.currentTimeMillis, 0L, 0L, 0L, 0L,"","",0L)
            }
          }
        }
      } ~
      path("list") {
        get {
          respondWithMediaType(`application/json`) { 
            complete {
              List("Sensor 1", "Sensor 2", "Sensor 3", "Sensor 4")
            }
          }
        }
      }
    } ~
    pathPrefix("sensor") {
      pathPrefix(Segment) { id =>
        path("info") {
          get {
            respondWithMediaType(`application/json`) { 
              complete {
                Sensor(System.currentTimeMillis, 0L, 0L, 0L, 0L,"","",0L)
              }
            }
          }
        } ~
        path("newest") {
          get {
            respondWithMediaType(`application/json`) { 
              complete {
                val future = sensorDataHandler ? AskLatestSensorMessage
                val result = Await.result(future, timeout.duration).asInstanceOf[Option[Sensor]]
                result
              }
            }
          }
        } ~
        path(LongNumber) { timestamp =>
          get {
            respondWithMediaType(`application/json`) { 
              complete {
                Sensor(System.currentTimeMillis, 0L, 0L, 0L, 0L,"","",0L)
              }
            }
          }
        }
      }
    } ~
    path("") {
      get {
        respondWithMediaType(`text/html`) { // XML is marshalled to `text/xml` by default, so we simply override here
          complete {
            <html>
              <body>
                <h1>Say hello to <i>spray-routing</i> on <i>spray-can</i>!</h1>
              </body>
            </html>
          }
        }
      }
    }
  }
}