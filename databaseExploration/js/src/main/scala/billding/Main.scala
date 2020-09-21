package billding

import billding.SQL.{Conjunction, Equal, GeneratedCode, InvalidStatement, Operator, PhysicalPlan, RelName, Statement, StatementValidation, TranslatedStatement, ValidatedStatement}
import com.raquo.airstream.signal.Signal
import org.scalajs.dom
import org.scalajs.dom.{Event, document, html}
import org.scalajs.dom.raw.AudioContext

import scala.scalajs.js.{Date, URIUtils}
import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.{ReactiveElement, ReactiveHtmlElement}
import fastparse.Parsed
import fastparse.Parsed.{Failure, Success}
import io.circe.Encoder
import io.circe.generic.auto._
import io.circe.syntax._

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
  case class Attribute(
    value: String
                    ) extends AnyVal
  case class RelName(
                      value: String
                    ) extends AnyVal
  case class Table(
    name: RelName,
                  )
  sealed trait Operator
  case object Equal extends Operator
  case object LessThan extends Operator
  case object GreaterThan extends Operator

  object Operator {

    def apply(raw: String): Operator = raw match {
      case "=" => Equal
      case "<" => LessThan
      case ">" => GreaterThan
      case unrecognizedOperator =>
        throw new RuntimeException("Unrecognized operator: " + unrecognizedOperator)
    }
  }
  sealed trait Conjunction
  case object AND extends Conjunction
  case object OR extends Conjunction

  object Conjunction {

    def apply(raw: String) = raw match {
      case "AND" => AND
      case "OR"  => OR
      case unrecognizedConjunction =>
        throw new RuntimeException("Unrecognized conjunction: " + unrecognizedConjunction)

    }
  }

  case class Condition(
    fieldName: String,
    operator: Operator,
    fieldValue: String
  )

  case class Statement(select: String,
                       columnName: Attribute,
                       from: String,
                       fromList: Seq[RelName],
                       conditionList: Option[(String, (Condition, Seq[(Conjunction, Condition)]))])

  sealed trait StatementValidation

  case class ValidatedStatement(
    statement: Statement
                               ) extends StatementValidation

  case class InvalidStatement(
                                 error: String
                               ) extends StatementValidation


  case class TranslatedStatement(
                                    statement: Statement
                                  )

  case class PhysicalPlan(
                           statement: Statement
                         )

  case class GeneratedCode(
    physicalPlan: PhysicalPlan
                          )

  object parsing {
    import fastparse._, MultiLineWhitespace._
    def select[_: P]: P[String] = P(IgnoreCase("SELECT").!)
    def columnName[_: P]: P[Attribute] = P(CharIn("a-z_").rep(1).!).map(Attribute)
    def from[_: P]: P[String] = P(IgnoreCase("FROM").!)
    def relName[_: P]: P[RelName] = P(CharIn("a-z_").rep(1).!).map(RelName)

    def fromList[_: P]: P[Seq[RelName]] = // This is actually nonEmpty.
      P(relName ~ ("," ~ relName).rep)
        .map { case (first, rest) => first +: rest }
    def WHERE[_: P]: P[String] = P(IgnoreCase("WHERE").!)

    def attribute[_: P]: P[String] = P(CharIn("a-z").rep(1).!)
    def operator[_: P]: P[String] = P(CharIn("=<>").!)

    def condition[_: P]: P[Condition] = P(attribute ~ operator ~ attribute).map {
      case (fieldName, operator, fieldValue) => Condition(fieldName, Operator(operator), fieldValue)
    }

    def compositeCondition[_: P]: P[(Conjunction, Condition)] =
      P(P("AND" | "OR").! ~ condition)
        .map { case (rawConjunction, condition) => (Conjunction(rawConjunction), condition) }

    def conditions[_: P]: P[(Condition, Seq[(Conjunction, Condition)])] =
      P(condition ~ compositeCondition.rep)

    def statement[_: P]: P[Statement] =
      P(select ~ columnName ~ from ~ fromList ~ (WHERE ~ conditions).? ~ End)
        .map {
          case (selectToken, columnName, fromToken, fromList, conditionList) =>
            Statement(selectToken, columnName, fromToken, fromList, conditionList)
        }

    def parseStatement(expression: String) =
      parse(expression, statement(_))

  }
}

object Main {
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

  def SqlStatementComponent(id: Int, displayCode: Binder[HtmlElement]): ReactiveHtmlElement[html.Div] = {
    val expressionBus = new EventBus[String]
    val parseResultStream: EventStream[Parsed[Statement]] =
      expressionBus.events.map(SQL.parsing.parseStatement)

    val definedTables = List(RelName("table_a"),  RelName("table_b"))
    val streamUpdateDelayMS = 100

    val parseValidationStream: EventStream[StatementValidation] =
      parseResultStream.map {
        case Success(value, index) if value.fromList.forall(definedTables.contains(_))  => ValidatedStatement(value)
        case Success(value, index) => InvalidStatement("Invalid references in syntactically-valid statement")
        case _: Failure => InvalidStatement("Failure in previous stage")
      }

    val translationStream: EventStream[Option[TranslatedStatement]] =
      parseValidationStream.delay(streamUpdateDelayMS).map{
        case ValidatedStatement(statement) => Some(TranslatedStatement(statement))
        case _: InvalidStatement => None
      }

    val physicalPlanStream: EventStream[Option[PhysicalPlan]] =
      translationStream.delay(streamUpdateDelayMS).map {
        case Some(translatedStatement) => Some(PhysicalPlan(translatedStatement.statement))
        case None => None
      }

    val generatedCodeStream: EventStream[Option[GeneratedCode]] =
      physicalPlanStream.delay(streamUpdateDelayMS).map {
        case Some(physicalPlan) => Some(GeneratedCode(physicalPlan))
        case None => None
      }

    val $color: Signal[String] =
      parseResultStream.foldLeft("red") { (prev, parseResult) =>
        if (parseResult.isSuccess) "green" else "red"
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
          textArea(
            cols := 100,
            rows := 6,
            inContext(thisNode => onInput.mapTo(thisNode.ref.value) --> expressionBus) // extract text entered into this input node whenever the user types in it
          )
        ),
        div(
          "Your Query: ",
          Hello(expressionBus.events, $color)
        ),
        div(styleAttr <-- $color.map(color => s"background: $color"), "success by color"),
        table(
          cls := "table is-bordered is-striped is-hoverable centered",
          caption("Parsing stages"),
          thead(
            tr(
              th("Stage"),
              th("Result"),
            )
          ),
          tbody(
            tr(
              td("Parse"),
              td(
                child <-- parseResultStream.map {
                  case Success(value, index) => "Syntactically-valid"
                  //              pre(textAlign := "left", result.get.value.asJson.spaces2) // TODO restore after dev
                  case failure: Failure => "Parse error: " + failure
                }
              )
            ),
            tr(
              td("Validation"),
              td(
                child <-- parseValidationStream.map(validationResult => div(
                  validationResult match {
                    case InvalidStatement(error) => div(error)
                    case ValidatedStatement(statement) => div("Matched with available tables")
                  }

                ))
              )
            ),
            tr(
              td("Translation"),
              td(
                child <-- translationStream.map {
                  case Some(translatedStatement) => div("Translated statement.")
                  case None => div("Failure in previous step")
                }
              )
            ),
            tr(
              td("Physical Plan"),
              td(
                child <-- physicalPlanStream.map {
                  case Some(physicalPlan) =>
                    div("Algorithms & Accessors for: " + physicalPlan.statement.fromList.map(_.value).mkString(",") )
                  case None => div("Failure in previous step")
                }
              )
            ),
            tr(
              td("Generate Code"),
              td(
                child <-- generatedCodeStream.map {
                  case Some(generatedCode) => div(Integer.toBinaryString(generatedCode.toString.length))
                  case None => div("Failure in previous step")
                }
              )
            )
          )
        ),
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
      SqlStatementComponent(1,
                       styleAttr <-- $selectedComponent.map(
                         selection => s"""display: ${if (selection == 1) "inline" else "none"}"""
                       ))
    )

    render(dom.document.querySelector("#laminarApp"), appDiv)
  }

  def main(args: Array[String]): Unit = {
    println("Anything 1")
    laminarStuff()

  }
}
