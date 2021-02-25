package billding

import billding.ExerciseSessionComponent.{
  ExerciseSessionComponentWithExternalStatus,
  TickingExerciseCounterComponent,
  TickingManualExerciseComponent
}
import com.billding.{FULL, OFF, SoundStatus}
import com.billding.exercises.{Exercise, Exercises}
import com.billding.settings.{Setting, SettingWithValue}
import com.raquo.airstream.eventbus.EventBus
import com.raquo.airstream.signal.Signal
import org.scalajs.dom
import org.scalajs.dom.{document, raw}
import org.scalajs.dom.raw.{AudioContext, Storage}
import com.raquo.laminar.api.L._
import org.scalajs.dom.experimental.serviceworkers.toServiceWorkerNavigator

import scala.scalajs.js
import scala.util.{Failure, Success}

private class Meta(document: org.scalajs.dom.html.Document) {

  val (host, path) =
    document.URL.split("/").splitAt(3) match {
      case (a, b) => (a.mkString("/"), b.mkString("/"))
    }

  val accessToken: Option[String] =
    if (document.URL.contains("?")) {
      val queryParameters =
        document.URL.split('?')(1)
      val tokenWithPossibleHash = queryParameters.replace("access_token=", "")
      val cleanToken =
        if (tokenWithPossibleHash.endsWith("#"))
          tokenWithPossibleHash.dropRight(1)
        else
          tokenWithPossibleHash
      val storage = org.scalajs.dom.window.localStorage
      storage.setItem("access_token_fromJS", cleanToken) // TODO Test this
      Some(cleanToken)
    } else {
      None
    }

}

object Main {
  var audioContext = new AudioContext()

  def laminarStuff(storage: Storage, appElement: raw.Element, meta: Meta) = {
    val apiClient = new ApiClient(meta.host, meta.accessToken)
    val allExerciseCounters = Var[Seq[(ExerciseSessionComponent, Boolean)]](Seq())
    val updateMonitor = Observer[(Boolean, Exercise)](onNext = {
      case (isComplete, exercise) =>
        allExerciseCounters.update(
          previousExerciseCounters =>
            previousExerciseCounters.map {
              case (currentExerciseComponent, wasComplete) => {
                val finalCompletionState =
                  if (currentExerciseComponent.exercise == exercise)
                    isComplete
                  else wasComplete

                (currentExerciseComponent, finalCompletionState)
              }
            }
        )
    })

    val componentSelections = new EventBus[Exercise]
    val $selectedComponent: Signal[Exercise] =
      componentSelections.events
        .foldLeft[Exercise](Exercises.kickCounter)((_, selection) => selection)

    val soundStatusEventBus = new EventBus[SoundStatus]
    val $soundStatus =
      soundStatusEventBus.events.foldLeft[SoundStatus](OFF) {
        case (oldStatus: SoundStatus, newStatus: SoundStatus) => {
          newStatus
        }
      }

    val betterExerciseComponents: Seq[ExerciseSessionComponent] =
      Exercises.manuallyCountedExercises
        .map(
          exercise =>
            new ExerciseSessionComponentWithExternalStatus(
              componentSelections.writer,
              exercise,
              $selectedComponent,
              apiClient.postExerciseSession,
              new SoundCreator, // TODO Encapuslate $soundStatus in SoundCreator
              storage,
              updateMonitor.contramap[Boolean](isComplete => (isComplete, exercise)),
              $soundStatus,
              apiClient
            )
        ) :+ TickingExerciseCounterComponent(componentSelections.writer,
                                             Exercises.QuadSets,
                                             $selectedComponent,
                                             apiClient.postExerciseSession,
                                             storage,
                                             new SoundCreator,
                                             $soundStatus,
                                             apiClient) :+ TickingManualExerciseComponent(
        componentSelections.writer,
        Exercises.kickCounter,
        $selectedComponent,
        apiClient.postExerciseSession,
        storage,
        new SoundCreator,
        $soundStatus,
        apiClient
      )

    allExerciseCounters.set(betterExerciseComponents.map((_, false)))

    val partitionedExercises: Signal[
      (Seq[(ExerciseSessionComponent, Boolean)], Seq[(ExerciseSessionComponent, Boolean)])
    ] =
      allExerciseCounters.signal.map(
        exerciseCounters =>
          exerciseCounters.partition { case (exerciseCounter, complete) => complete }
      )

    val $completedExercises: Signal[Seq[ExerciseSessionComponent]] =
      partitionedExercises.map(_._1.map(_._1))
    val $incompleteExercises: Signal[Seq[ExerciseSessionComponent]] =
      partitionedExercises.map(_._2.map(_._1))

    import scala.concurrent.ExecutionContext.Implicits.global
    val appDiv: Div = div(
      idAttr := "full_laminar_app",
      cls := " has-text-centered",
      soundStatusEventBus.events --> Observer[SoundStatus] { (nextValue: SoundStatus) =>
        println("About to submit a new soundstatus")
        apiClient.postUserSetting(
          SettingWithValue(Setting("SoundStatus"), nextValue.toString)
        )
      },
      EventStream
        .fromFuture(
          apiClient
            .getUserSetting(
              Setting("SoundStatus")
            )
            .map { settingWithValue =>
              if (settingWithValue.value == "FULL") FULL else OFF
            }
        ) --> soundStatusEventBus.writer,
      Bulma.menu(
        $completedExercises,
        $incompleteExercises,
        soundStatusEventBus.writer,
        soundStatusEventBus.events.toSignal(OFF)
      ),
      betterExerciseComponents.map(_.exerciseSessionComponent)
    )

    println("going to render laminarApp sunday 10:40")

    appElement.innerHTML = "" // Ugly emptying method
    render(appElement, appDiv)
  }

  def main(args: Array[String]): Unit = {
    val meta = new Meta(document)
    laminarStuff(org.scalajs.dom.window.localStorage,
                 dom.document.querySelector("#laminarApp"),
                 meta)

    if (meta.accessToken.isDefined) {
      dom.window.location.href =
        "https://purelyfunctionalserver.herokuapp.com/resources/html/PhysicalTherapyTracker/index.html"
    }

    import scala.concurrent.ExecutionContext.Implicits.global

    toServiceWorkerNavigator(org.scalajs.dom.window.navigator).serviceWorker
      .register("./sw-opt.js", js.Dynamic.literal(scope = "./"))
      .toFuture
      .onComplete {
        case Success(registration) =>
          registration.update()
        case Failure(error) =>
          println(
            s"registerServiceWorker: service worker registration failed > ${error.printStackTrace()}"
          )
      }
  }
}
