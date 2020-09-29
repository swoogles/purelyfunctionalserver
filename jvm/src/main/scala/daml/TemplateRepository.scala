package daml

import doobie.util.transactor.Transactor
import zio.Task
import doobie.implicits._
import zio.interop.catz._
import doobie._
import doobie.implicits._
import doobie.util.ExecutionContexts

trait TemplateRepository {
  def createTableFor(template: Template): Task[Unit]
}

class TemplateRepositoryImpl(transactor: Transactor[Task]) extends TemplateRepository {
  override def createTableFor(template: Template): Task[Unit] = {
    val fragment = fr"CREATE TABLE " ++ Fragment.const("daml." + template.name) ++
      fr" ( " ++
      Fragment.const(template.id + " SERIAL, ") ++
      UserDefinedColumn.convertToSqlColumnDefinitions(template.arguments) ++
      Fragment.const(", " + template.signatories.map(ColumnConversions.convertParty).mkString(", ")) ++ fr" )"

    println("Fragment: ")
    println(fragment)

    fragment
      .update
      .run
      .transact(transactor)
      .map(id => ())

//    sql"""CREATE TABLE IF NOT EXISTS $tableName ( $fieldNames TEXT[] )""".update.run
//      .transact(transactor)
//      .map(id => ())

  }
}
