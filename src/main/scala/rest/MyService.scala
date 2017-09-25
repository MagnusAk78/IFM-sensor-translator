package rest

import akka.actor.Actor
import spray.routing._
import spray.http._
import MediaTypes._

import model.SensorIntegerJsonSupport._
import model.SensorInteger
import spray.json._

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class MyServiceActor extends Actor with MyService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(myRoute)
}


// this trait defines our service behavior independently from the service actor
trait MyService extends HttpService {

  val myRoute = {
    import spray.httpx.SprayJsonSupport.sprayJsonMarshaller
    import spray.httpx.SprayJsonSupport.sprayJsonUnmarshaller
    
    pathPrefix("sensors") {
      path("info") {
        get {
          respondWithMediaType(`application/json`) { 
            complete {
              List(SensorInteger("Sensor 1", 5, System.currentTimeMillis),
                SensorInteger("Sensor 2", 5, System.currentTimeMillis),
                SensorInteger("Sensor 3", 5, System.currentTimeMillis),
                SensorInteger("Sensor 4", 5, System.currentTimeMillis))
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
                SensorInteger(id, 34, System.currentTimeMillis)
              }
            }
          }
        } ~
        path("newest") {
          get {
            respondWithMediaType(`application/json`) { 
              complete {
                SensorInteger(id, 34, System.currentTimeMillis)
              }
            }
          }
        } ~
        path(LongNumber) { timestamp =>
          get {
            respondWithMediaType(`application/json`) { 
              complete {
                SensorInteger(id, 34, System.currentTimeMillis)
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