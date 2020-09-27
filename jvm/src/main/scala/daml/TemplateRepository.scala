package daml

import doobie.util.transactor.Transactor
import zio.Task
import doobie.implicits._
import zio.interop.catz._

trait TemplateRepository {
  def createTableFor[T](template: Template[T]): Task[Unit]
}

class TemplateRepositoryImpl(transactor: Transactor[Task]) extends TemplateRepository {
  override def createTableFor[T](template: Template[T]): Task[Unit] = {
    val dynamicFieldName = template.arguments.head
    println("Going to insert: " + template)
    println("dynamicFieldName: " + dynamicFieldName)

      sql"""
    CREATE TABLE IF NOT EXISTS ${template.name} ( signatories TEXT[] )
  """.update.run
        .transact(transactor)
        .map(id => ())

  }
}
