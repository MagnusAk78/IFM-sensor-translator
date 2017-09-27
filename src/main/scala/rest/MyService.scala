package rest

import akka.actor.{ Actor, ActorRef }
import scala.concurrent.Await
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import spray.routing._
import spray.http._
import MediaTypes._

import spray.json._

import model.IfmJsonSupport._
import model.ProcessData
import model.SensorInfo
import model.SensorStatus
import model.ErrorMessage
import control._

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
              val future = sensorDataHandler ? AskSensorsInfoMessage
              val result = Await.result(future, timeout.duration).asInstanceOf[List[SensorInfo]]
              result
            }
          }
        }
      } ~
        path("list") {
          get {
            respondWithMediaType(`application/json`) {
              complete {
                val future = sensorDataHandler ? AskSensorsListMessage
                val result = Await.result(future, timeout.duration).asInstanceOf[List[String]]
                result
              }
            }
          }
        } ~
        path("status") {
          get {
            respondWithMediaType(`application/json`) {
              complete {
                val future = sensorDataHandler ? AskSensorsStatusMessage
                val result = Await.result(future, timeout.duration).asInstanceOf[List[SensorStatus]]
                result
              }
            }
          }
        }
    } ~
      pathPrefix("sensor") {
        pathPrefix(Segment) { id: String =>
          path("info") {
            get {
              respondWithMediaType(`application/json`) {
                complete {
                  val future = sensorDataHandler ? AskSensorInfoMessage(id)
                  val result = Await.result(future, timeout.duration).asInstanceOf[Option[SensorInfo]]
                  result match {
                    case Some(sensorInfo) => sensorInfo
                    case None => ErrorMessage("Requested data was not found")
                  }
                }
              }
            }
          } ~
            path("last") {
              get {
                respondWithMediaType(`application/json`) {
                  complete {
                    val future = sensorDataHandler ? AskLastProcessDataMessage(id)
                    val result = Await.result(future, timeout.duration).asInstanceOf[Option[ProcessData]]
                    result match {
                      case Some(sensorInfo) => sensorInfo
                      case None => ErrorMessage("Requested data was not found")
                    }
                  }
                }
              }
            } ~
            path(LongNumber) { timestamp =>
              get {
                respondWithMediaType(`application/json`) {
                  complete {
                    val future = sensorDataHandler ? AskProcessDataAfterTimestampMessage(id, timestamp)
                    val result = Await.result(future, timeout.duration).asInstanceOf[List[ProcessData]]
                    result
                  }
                }
              }
            } ~
            path("status") {
              get {
                respondWithMediaType(`application/json`) {
                  complete {
                    val future = sensorDataHandler ? AskSensorStatusMessage(id)
                    val result = Await.result(future, timeout.duration).asInstanceOf[Option[SensorStatus]]
                    result match {
                      case Some(sensorInfo) => sensorInfo
                      case None => ErrorMessage("Requested data was not found")
                    }
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
              """
              <html>
                <body>
                  <h1>IMF sensor service</h1>
									<br>
									/sensors/info <br>
									/sensors/list <br>
									/sensors/status <br>
									/sensor/id:String/info <br>
									/sensor/id:String/last => Get last received process data<br>
									/sensor/id:String/timestamp:Long => Get all process data after timestamp<br>
									/sensor/id:String/status <br>
                </body>
              </html>
              """
            }
          }
        }
      }
  }
}