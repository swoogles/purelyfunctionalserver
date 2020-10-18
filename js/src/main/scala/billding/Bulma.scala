package billding

import com.raquo.laminar.api.L._
//import com.raquo.laminar.api.Laminar.aria
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom
import org.scalajs.dom.html

object Bulma {

  def menu(choices: List[ReactiveHtmlElement[html.Div]]) = {
    val menuClicks = new EventBus[dom.Event]

    val activeStyling =
      menuClicks.events.foldLeft("")(
        (acc, next) => if (!acc.contains("is-active")) "is-active" else ""
      )

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
          div(
            cls := "navbar-item has-dropdown is-hoverable",
            a(onClick --> menuClicks, cls("navbar-link centered"), "Exercises"),
            div(cls("navbar-dropdown"), choices.map { choice =>
              choice.ref.classList.add("navbar-item"); div(onClick --> menuClicks, choice)
            })
          )
        ),
        div(cls("navbar-end"))
      )
    )
  }
}
