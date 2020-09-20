package billding

import com.raquo.airstream.signal.Signal
import org.scalajs.dom
import org.scalajs.dom.{document, html, Event}
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

object SQL {
  sealed trait Operator
  case object Equal extends Operator
  case object LessThan extends Operator
  case object GreaterThan extends Operator
  object Operator {
    def apply(raw: String): Operator = raw match {
      case "=" => Equal
      case "<" => LessThan
      case ">" => GreaterThan
      case unrecognizedOperator => throw new RuntimeException("Unrecognized operator: " + unrecognizedOperator)
    }
  }
  sealed trait Conjunction
  case object AND extends Conjunction
  case object OR extends Conjunction
  object Conjunction {
    def apply(raw: String) = raw match {
      case "AND" => AND
      case "OR" => OR
      case unrecognizedConjunction => throw new RuntimeException("Unrecognized conjunction: " + unrecognizedConjunction)

    }
  }

  case class Condition(
    fieldName: String, operator: Operator, fieldValue: String
                      )

  object parsing {
    import fastparse._, MultiLineWhitespace._
    def select[_: P]: P[String] = P(IgnoreCase("SELECT").!)
    def columnName[_: P]: P[String] = P(CharIn("a-z").rep(1).!)
    def from[_: P]: P[String] = P(IgnoreCase("FROM").!)
    def relName[_: P]: P[String] = P(CharIn("a-z").rep(1).!)
    def fromList[_: P]: P[Seq[String]] =  // This is actually nonEmpty.
      P(relName.! ~ ("," ~ relName.!).rep)
      .map{ case (first, rest) => first +: rest}
    def WHERE[_: P]: P[String] = P(IgnoreCase("WHERE").!)

    def attribute[_: P]: P[String] = P(CharIn("a-z").rep(1).!)
    def operator[_: P]: P[String] = P(CharIn("=<>").!)
    def condition[_: P]: P[Condition] = P(attribute ~ operator ~ attribute).map {
      case (fieldName, operator, fieldValue) => Condition(fieldName, Operator(operator), fieldValue)
    }
    def compositeCondition[_: P]: P[(Conjunction, Condition)] =
      P(P("AND" | "OR").! ~ condition)
      .map{ case (rawConjunction, condition) => (Conjunction(rawConjunction), condition)}
    def conditions[_: P]: P[(Condition, Seq[(Conjunction, Condition)])] =
      P(condition ~ compositeCondition.rep)
//      .map{ case (condition, conditionsAndConjunctions: Seq[(Conjunction, Condition)]) => (condition, Condition(???, ???, ???))}
    def statement[_: P]: P[(String, String, String, Seq[String], Option[(String, (Condition, Seq[(Conjunction, Condition)]))])] =
      P(select ~ columnName ~ from ~ fromList ~ (WHERE ~ conditions).? ~ End)

    def parseStatement(expression: String) =
      parse(expression, statement(_))
//    def number[_: P]: P[Int] = P( CharIn("a-z").rep(1).!.map(_.toInt) )
//    def parens[_: P]: P[Int] = P( "(" ~/ addSub ~ ")" )
//    def factor[_: P]: P[Int] = P( number | parens )

//    def divMul[_: P]: P[Int] = P( factor ~ (CharIn("*/").! ~/ factor).rep )
//    def addSub[_: P]: P[Int] = P( divMul ~ (CharIn("+\\-").! ~/ divMul).rep )
//    def expr[_: P]: P[Int]   = P( addSub ~ End )
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
        case ResetCount           => Counter(0)
        case increment: Increment => counter.copy(counter.value + increment.value)
      }
  }

  case class RepeatingElement() extends RepeatWithIntervalHelper

  def ArmStretchComponent(id: Int, displayCode: Binder[HtmlElement]) = {
    val armStretches = new EventBus[Int]
    val $shoulderStretchTotal: Signal[Int] =
      armStretches.events.foldLeft(0)((acc, next) => acc + next)

    div(
      idAttr := s"counter_component_$id",
      displayCode,
      cls("centered"),
      div(
        cls("session-counter"),
        div(cls := "medium", "Shoulder Stretches:"),
        div(idAttr := "shoulder_stretches_daily_total",
            child <-- $shoulderStretchTotal.map(count => div(count.toString)))
      ),
      div(
        cls := "centered",
        button(
          cls := "button is-link is-rounded medium",
          onClick.mapTo(value = { -1 }) --> armStretches,
          "-1"
        ),
        button(
          cls := "button is-link is-rounded medium",
          onClick.mapTo(value = { 1 }) --> armStretches,
          "+1"
        )
      )
    )

  }

  def CounterComponent(id: Int, displayCode: Binder[HtmlElement]): ReactiveHtmlElement[html.Div] = {
    val nameBus = new EventBus[String]
    val $color: Signal[String] =
      nameBus.events.foldLeft("red") { (prev, next) =>
        if (SQL.parsing.parseStatement(next).isSuccess) "green" else "red"
      }

    val diffBusT = new EventBus[CounterAction]()
    val $countT: Signal[Counter] =
      diffBusT.events.foldLeft(Counter(0))((acc, next) => CounterAction.update(next, acc))

    div(
      displayCode,
      idAttr := s"counter_component_$id",
      cls := "centered",
      div(
        div(
          "Please enter your query:",
          input(
            typ := "text",
            inContext(thisNode => onInput.mapTo(thisNode.ref.value) --> nameBus) // extract text entered into this input node whenever the user types in it
          )
        ),
        div(
          "Your Query: ",
          Hello(nameBus.events, $color)
        ),
        div(styleAttr <-- $color.map(color => s"background: $color"), "success by color"),
        button("Reset",
               cls := "button is-warning is-rounded medium",
               onClick.mapTo(ResetCount) --> diffBusT)
      )
    )
  }

  def Hello(
    helloNameStream: EventStream[String],
    helloColorStream: Signal[String]
  ): Div =
    div(
      fontSize := "20px", // static CSS property
      color <-- helloColorStream, // dynamic CSS property
      strong("Hello, "), // static child element with a grandchild text node
      child.text <-- helloNameStream // dynamic child (text node in this case)
    )

  def laminarStuff() = {
    val nameBus = new EventBus[String]
    val colorStream: EventStream[String] = nameBus.events.map { name =>
      if (name == "Sébastien") "red" else "unset" // make Sébastien feel special
    }

    val componentSelections = new EventBus[Int]
    val $selectedComponent: Signal[Int] =
      componentSelections.events.foldLeft(1)((_, selection) => selection)

    val appDiv: Div = div(
      idAttr := "full_laminar_app",
      cls := "centered",
      button("QuadSets",
             cls := "button is-primary is-rounded small",
             onClick.mapTo(1) --> componentSelections),
      button("Shoulder Stretches",
             cls := "button is-primary is-rounded small",
             onClick.mapTo(2) --> componentSelections),
//      h1("User Welcomer 9000"),
      CounterComponent(1,
                       styleAttr <-- $selectedComponent.map(
                         selection => s"""display: ${if (selection == 1) "inline" else "none"}"""
                       )
      ),
      ArmStretchComponent(2,
                          styleAttr <-- $selectedComponent.map(
                            selection => s"""display: ${if (selection == 2) "inline" else "none"}"""
                          )
      )
    )

    render(dom.document.querySelector("#laminarApp"), appDiv)
  }

  def main(args: Array[String]): Unit = {
    println("Anything 1")
    laminarStuff()

  }
}
