package billding

import com.raquo.airstream.signal.Signal
import org.scalajs.dom
import org.scalajs.dom.{Event, document, html}
import org.scalajs.dom.raw.AudioContext

import scala.scalajs.js.{Date, URIUtils}
import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.{ReactiveElement, ReactiveHtmlElement}

import scala.concurrent.duration.FiniteDuration

case class DailyQuantizedExercise(name: String, day: String, count: Int)

object Time {

  def formattedLocalDate(): String = {
    val jsDate = new Date()
    val monthSection =
      if (jsDate.getMonth() + 1 > 9)
        (jsDate.getMonth() + 1).toString
      else
        "0" + (jsDate.getMonth() + 1).toString

    val daySection =
      if (jsDate.getDate() > 9)
        jsDate.getDate().toString
      else
        "0" + jsDate.getDate().toString

    jsDate.getFullYear().toString + "-" + monthSection + "-" + daySection
  }

}

object Main {
  var count = 0
  var dailyTotal = 0
  var shoulderStretchTotal = 0
  var audioContext = new AudioContext()

  case class Counter(value: Int)
  sealed trait CounterAction
  case object ResetCount extends CounterAction
  case class Increment(value: Int) extends CounterAction

  object CounterAction {
    def update(counterAction: CounterAction, counter: Counter) =
      counterAction match {
        case ResetCount => Counter(0)
        case increment: Increment => counter.copy(counter.value+increment.value)
      }
  }

  case class RepeatingElement () extends RepeatWithIntervalHelper

  def ArmStretchComponent(id: Int, displayCode: Binder[HtmlElement]) = {
    val armStretches = new EventBus[Int]
    val $shoulderStretchTotal: Signal[Int] = armStretches.events.foldLeft(0)((acc, next) => acc+next)

    div(
      idAttr:=s"counter_component_$id",
      displayCode,
      cls("centered"),
      div(
        cls("session-counter"),
        div(cls:="medium", "Shoulder Stretches:"),
        div(idAttr:="shoulder_stretches_daily_total",
          child <-- $shoulderStretchTotal.map(count => div(count.toString)))
      ),
      div(
        cls := "centered",
        button(
        cls := "button is-link is-rounded medium",
        onClick.mapTo(value ={-1})  --> armStretches,
        "-1",
      ),
      button(
        cls := "button is-link is-rounded medium",
        onClick.mapTo(value ={1})  --> armStretches,
        "+1",
      ),
      )
    )

  }

  def CounterComponent(id: Int, displayCode: Binder[HtmlElement]): ReactiveHtmlElement[html.Div] = {
    val repeater = RepeatingElement()

    val clockTicks = new EventBus[Int]
    val $color: Signal[String] = clockTicks.events.foldLeft("green")((color, _) => if(color=="red") "green" else "red")
    val diffBusT =  new EventBus[CounterAction]()
    val $countT: Signal[Counter] = diffBusT.events.foldLeft(Counter(0))((acc, next) =>
      CounterAction.update(next, acc)
    )

    val duration = new FiniteDuration(10, scala.concurrent.duration.SECONDS)

    div(
      displayCode,
      idAttr:=s"counter_component_$id",
      cls:="centered",
      div(
      styleAttr <-- $color.map(color=> s"background: $color"),
      div(cls("session-counter"), child.text <-- $countT.map(_.value.toString)),
      button("Submit",
        cls := "button is-link is-rounded",
        dataAttr("count") <-- $countT.map(_.value.toString),
        inContext( context =>
          onClick.mapTo(ResetCount) --> diffBusT)),
      button("Reset",
        cls := "button is-warning is-rounded medium",
        onClick.mapTo(ResetCount) --> diffBusT),
      div(styleAttr:="font-size: 4em", cls:="box",
        span(
        "Play Sounds:"),
        input(typ:="checkbox",idAttr:="play-audio",name:="play-audio",value:="true")
      ),
      div(idAttr:="daily_total_section", styleAttr:="text-align: center; font-size: 2em",
        span("Daily Total:"),
        span(idAttr:="daily_total", styleAttr:="font-size: 2em")
      ),
      a(href:="/", cls := "button is-link is-rounded medium", "Re-login"),
      div(idAttr:="exercise_history"),

      repeater.repeatWithInterval(
        Increment(1).asInstanceOf[CounterAction],
        duration*2
//        new FiniteDuration(20, scala.concurrent.duration.SECONDS) // todo restore after dev
    ) --> diffBusT,
        repeater.repeatWithInterval(
      1,
          duration
      //        new FiniteDuration(20, scala.concurrent.duration.SECONDS) // todo restore after dev
    ) --> clockTicks
    )
    )
  }

  def Hello(
             helloNameStream: EventStream[String],
             helloColorStream: EventStream[String]
           ): Div = {
    div(
      fontSize := "20px", // static CSS property
      color <-- helloColorStream, // dynamic CSS property
      strong("Hello, "), // static child element with a grandchild text node
      child.text <-- helloNameStream // dynamic child (text node in this case)
    )
  }

  def laminarStuff() = {
    val nameBus = new EventBus[String]
    val colorStream: EventStream[String] = nameBus.events.map { name =>
      if (name == "Sébastien") "red" else "unset" // make Sébastien feel special
    }

    val componentSelections = new EventBus[Int]
    val $selectedComponent: Signal[Int] = componentSelections.events.foldLeft(1)((_, selection) => selection)

    val appDiv: Div = div(
      idAttr:="full_laminar_app",
      cls := "centered",
      button("QuadSets",
        cls := "button is-primary is-rounded small",
        onClick.mapTo(1) --> componentSelections),
      button("Shoulder Stretches",
        cls := "button is-primary is-rounded small",
        onClick.mapTo(2) --> componentSelections),
//      h1("User Welcomer 9000"),
//      div(
//        "Please enter your name:",
//        input(
//          typ := "text",
//          inContext(thisNode => onInput.mapTo(thisNode.ref.value) --> nameBus) // extract text entered into this input node whenever the user types in it
//        )
//      ),
//      div(
//        "Please accept our greeting: ",
//        Hello(nameBus.events, colorStream)
//      ),
      CounterComponent(1,
        styleAttr <-- $selectedComponent.map(selection => s"""display: ${if (selection == 1) "inline" else "none" }""") ,
      ),
      ArmStretchComponent(2,
          styleAttr <-- $selectedComponent.map(selection => s"""display: ${if (selection == 2) "inline" else "none" }"""),
        )
    )

    render(dom.document.querySelector("#laminarApp"), appDiv)
  }

  def main(args: Array[String]): Unit = {
    println("No more stupid file copies and separate projects!!")
    laminarStuff()

    println("Cookie: " + document.cookie)
    val storage = org.scalajs.dom.window.localStorage
    // dom.window.location.href = "https://purelyfunctionalserver.herokuapp.com/resources/html/index.html"
  }
}
