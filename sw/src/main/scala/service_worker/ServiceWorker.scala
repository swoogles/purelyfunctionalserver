package service_worker

import org.scalajs.dom.experimental.Fetch._
import org.scalajs.dom.experimental._
import org.scalajs.dom.experimental.serviceworkers.ServiceWorkerGlobalScope._
import org.scalajs.dom.experimental.serviceworkers.{ExtendableEvent, FetchEvent}
import org.scalajs.dom.raw.MessageEvent

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.JSConverters._

object ServiceWorker {
  val todoCache = "cb-bus"

  val todoAssets: js.Array[RequestInfo] = List[RequestInfo](
//    "/",
//    "/index.html",
//    "/sw-opt.js",
    "images/BILLDING_LogoMark-256.png",
    "images/BILLDING_LogoMark-500.png",
    "images/BILLDING_LogoMark-1000.png",
    "index.html",
    "compiledJavaScript/quadset-counter-opt.js",
    "/resources/audio/goalReached/109662__grunz__success.wav",
    "/resources/audio/startQuadSet/metronome_tock.wav",
//    "https://cdn.jsdelivr.net/npm/bulma@0.9.0/css/bulma.min.css",
  ).toJSArray

  def main(args: Array[String]): Unit = {
    println("about to add install listener")
    self.addEventListener(
      "install",
      (event: ExtendableEvent) => {
//        println(
//          s"install: service worker with message handler installed > ${event.toString}"
//        )
        event.waitUntil(toCache().toJSPromise)
      }
    )

    println("about to add activate listener")
    self.addEventListener(
      "activate",
      (event: ExtendableEvent) => {
//        println(
//          s"activate: service worker activated > ${event.toString}"
//        )
        invalidateCache() // TODO Do I need this at all?
//      event.waitUntil(toCache().toJSPromise)
        self.clients.claim()
      }
    )

    println("about to add message listener")
    self.addEventListener(
      "message",
      (event: MessageEvent) => {
        self.registration.showNotification(
          s"This is a notification from the service worker!",
          NotificationOptions(
            vibrate = js.Array(100d)
          )
        )
        //        println(
        //          s"message received:  ${event.data}"
//        )
      }
    )

    self.addEventListener(
      "fetch",
      (event: FetchEvent) => {
        event.respondWith(
          fromCache(event.request)
            .recoverWith{ case error => fetch(event.request).toFuture}
            .toJSPromise
        )

        /*
        if (event.request.cache == RequestCache.`only-if-cached`
            && event.request.mode != RequestMode.`same-origin`) {
          println(
            s"fetch: Bug [823392] cache === only-if-cached && mode !== same-orgin' > ${event.request.url}"
          )
        } else {
          fromCache(event.request).onComplete {
            case Success(response) =>
              println(s"fetch: in cache > ${event.request.url}")
              response
            case Failure(error) =>
              println(
                s"fetch: not in cache, calling server... > ${event.request.url} > ${error.printStackTrace()}"
              )
              fetch(event.request).toFuture
                .onComplete {
                  case Success(response) => response
                  case Failure(finalError) =>
                    println(
                      s"fetch: final fetch failed > ${finalError.printStackTrace()}"
                    )
                }
          }
          3
        }

         */
      }
    )

    println("main: ServiceWorker installing...")
  }

  def toCache(): Future[Unit] = {
    self.caches
      .open(todoCache)
      .toFuture
      .flatMap{cache =>
                  println("toCache: caching assets...")
        cache.addAll(todoAssets).toFuture}
  }

  def fromCache(request: Request): Future[Response] =
    self.caches
      .`match`(request)
      .toFuture
      .flatMap { case response: Response =>
        if ( request.url.contains("index")) println(s"fromCache: matched request > ${request.url}")
        Future.successful(response)
      case other => Future.failed(new Exception("Could not find cached request"))
      }

  def invalidateCache(): Unit = {
    self.caches
      .delete(todoCache)
      .toFuture
      .map { invalidatedCache =>
        if (invalidatedCache) {
//          println(
//            s"invalidateCache: cache invalidated!', $invalidatedCache"
//          )
          toCache()
        }
      }
    ()
  }
}
