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
import model.SensorInfo
import spray.json._

import control.AskSensorInfoMessage
import control.AskLatestSensorMessage
import control.AskTimestampSensorMessage
import control.AskSensorListMessage


// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class MyServiceActor(override val sensorDataHandler: ActorRef) extends Actor with MyService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  def receive = runRoute(myRoute)
}

trait MyService extends HttpService {

  val sensorDataHandler: ActorRef
  
  implicit val timeout = Timeout(5 seconds)

  val myRoute = {
    import spray.httpx.SprayJsonSupport.sprayJsonMarshaller
    import spray.httpx.SprayJsonSupport.sprayJsonUnmarshaller

    pathPrefix("sensors") {
      path("info") {
        get {
          respondWithMediaType(`application/json`) {
            complete {
              Sensor(System.currentTimeMillis, 0L, 0L, 0L, 0L, "", "", 0L)
            }
          }
        }
      } ~
        path("list") {
          get {
            respondWithMediaType(`application/json`) {
              complete {
                val future = sensorDataHandler ? AskSensorListMessage
                val result = Await.result(future, timeout.duration).asInstanceOf[List[Long]]
                result
              }
            }
          }
        }
    } ~
      pathPrefix("sensor") {
        pathPrefix(LongNumber) { id: Long =>
          path("info") {
            get {
              respondWithMediaType(`application/json`) {
                complete {
                  val future = sensorDataHandler ? AskSensorInfoMessage(id)
                  val result = Await.result(future, timeout.duration).asInstanceOf[SensorInfo]
                  result
                }
              }
            }
          } ~
            path("newest") {
              get {
                respondWithMediaType(`application/json`) {
                  complete {
                    val future = sensorDataHandler ? AskLatestSensorMessage(id)
                    val result = Await.result(future, timeout.duration).asInstanceOf[Option[Sensor]]
                    result.get
                  }
                }
              }
            } ~
            path(LongNumber) { timestamp =>
              get {
                respondWithMediaType(`application/json`) {
                  complete {
                    val future = sensorDataHandler ? AskTimestampSensorMessage(id, timestamp)
                    val result = Await.result(future, timeout.duration).asInstanceOf[List[Sensor]]
                    result
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
                  <h1>IMF sensor service</h1>
                </body>
              </html>
            }
          }
        }
      }
  }
}