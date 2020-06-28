package auth

import java.util

import javax.servlet.ServletContext
import javax.servlet.http.{HttpSession, HttpSessionContext}

import scala.collection.mutable

class ScalaHttpSession extends HttpSession {
  val sessionVars = mutable.Map[String, Any]()
  override def getCreationTime: Long = ???

  override def getId: String = ???

  override def getLastAccessedTime: Long = ???

  override def getServletContext: ServletContext = ???

  override def setMaxInactiveInterval(interval: Int): Unit = ???

  override def getMaxInactiveInterval: Int = ???

  override def getSessionContext: HttpSessionContext = ???

  override def getAttribute(name: String): AnyRef =
    sessionVars.get(name)

  override def getValue(name: String): AnyRef = ???

  override def getAttributeNames: util.Enumeration[String] = ???

  override def getValueNames: Array[String] = ???

  override def setAttribute(name: String, value: Any): Unit = {
    println(s"Setting session attribute. Name: $name value: $value")
    sessionVars.put(name, value)
  }

  override def putValue(name: String, value: Any): Unit = ???

  override def removeAttribute(name: String): Unit = ???

  override def removeValue(name: String): Unit = ???

  override def invalidate(): Unit = ???

  override def isNew: Boolean = ???
}
