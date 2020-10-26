package billding

import billding.Main.ExerciseSessionComponentWithExternalStatus
import com.raquo.laminar.api.L
import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom
import org.scalajs.dom.html

object Bulma {

  def menu($completedExercises: Signal[Seq[ExerciseSessionComponentWithExternalStatus]],
           $incompleteExercises: Signal[Seq[ExerciseSessionComponentWithExternalStatus]],
           quadSetCounter: ReactiveHtmlElement[html.Div]): ReactiveHtmlElement[html.Div] = {
    val menuClicks = new EventBus[dom.Event]

    val activeStyling =
      menuClicks.events.foldLeft("")(
        (acc, next) => if (!acc.contains("is-active")) "is-active" else ""
      )

    val $completedExerciseButtons: Signal[Seq[ReactiveHtmlElement[html.Div]]] =
      $completedExercises.map(_.map(_.exerciseSelectButton()))

    val $completedRenderedButtons =
      $completedExerciseButtons.map { exerciseList =>
        div(exerciseList.map { choice =>
          choice.ref.classList.add("navbar-item"); div(onClick --> menuClicks, choice)
        })
      }

    val $incompleteExerciseButtons: Signal[Seq[ReactiveHtmlElement[html.Div]]] =
      $incompleteExercises.map(_.map(_.exerciseSelectButton()))

    val $incompleteRenderedButtons =
      $incompleteExerciseButtons.map { exerciseList =>
        div(exerciseList.map { choice =>
          choice.ref.classList.add("navbar-item"); div(onClick --> menuClicks, choice)
        })
      }

    div(
      idAttr := "main-menu",
      cls := "navbar",
      role := "navigation",
      aria.label := "main navigation",
      div(
        cls := "navbar-brand",
        a(
          role := "button",
          cls := "navbar-burger burger",
          onClick --> menuClicks,
          aria.label := "menu",
          aria.expanded(false),
          dataAttr("target") := "navbarBasicExample",
          span(aria.hidden(true)),
          span(aria.hidden(true)),
          span(aria.hidden(true))
        )
      ),
      div(
        idAttr := "navbarBasicExample",
        cls := "navbar-menu",
        cls <-- activeStyling,
        div(
          cls := "navbar-start",
          a(href := "/oauth/login", cls := "button is-link is-rounded medium", "Re-login"),
          child <-- $incompleteExercises.map { incompleteExercises =>
            if (incompleteExercises.nonEmpty) {
              div(
                cls := "navbar-item has-dropdown is-hoverable",
                a(onClick --> menuClicks, cls("navbar-link centered"), "Incomplete Exercises"),
                div(cls("navbar-dropdown"), child <-- $incompleteRenderedButtons)
              )
            } else {
              div()
            }
          },
          child <-- $completedExercises.map { completedExercises =>
            if (completedExercises.nonEmpty) {
              div(
                cls := "navbar-item has-dropdown is-hoverable",
                a(onClick --> menuClicks, cls("navbar-link centered"), "Completed Exercises"),
                div(cls("navbar-dropdown"), child <-- $completedRenderedButtons)
              )
            } else {
              div()
            }
          },
          div(
            cls := "navbar-item has-dropdown is-hoverable",
            a(onClick --> menuClicks, cls("navbar-link centered"), "Timed Exercises"),
            div(cls("navbar-dropdown"), quadSetCounter)
          )
        ),
        div(cls("navbar-end"))
      )
    )
  }
}
