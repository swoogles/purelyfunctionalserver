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
  def insert(contract: Contract): Task[Unit]
}

class TemplateRepositoryImpl(transactor: Transactor[Task]) extends TemplateRepository {

  override def createTableFor(template: Template): Task[Unit] = {
    val contractColumns =
      fr" id SERIAL, agreement_text TEXT, "

    val fragment = fr"CREATE TABLE " ++ Fragment.const("daml." + template.name) ++
      fr" ( " ++
      contractColumns ++
      UserDefinedColumn.convertToSqlColumnDefinitions(template.arguments) ++
      Fragment.const(", " + template.signatories.map(ColumnConversions.convertParty).mkString(", ")) ++ fr" )"

    println("Fragment: ")
    println(fragment)

    fragment.update.run
      .transact(transactor)
      .map(id => ())

//    sql"""CREATE TABLE IF NOT EXISTS $tableName ( $fieldNames TEXT[] )""".update.run
//      .transact(transactor)
//      .map(id => ())

  }

  override def insert(contract: Contract): Task[Unit] = {
    val columnNames = fr"id, agreement_text, " ++ Fragment.const(
        contract.template.arguments.map(_.name).mkString(",") + ","
      ) ++
      Fragment.const(contract.template.signatories.mkString(","))
    val valueList: Seq[String] =
      List(contract.contractId, contract.agreementText)
        .concat(contract.userDefinedColumnValues.map(_.value)) //todo ugh, toString
        .concat(contract.signatories)
        .map("'" + _ + "'")

    val fullGoal =
      Fragment.const(s"INSERT INTO daml.${contract.template.name}  ") ++
      fr"(" ++ columnNames ++ Fragment.const(") VALUES (") ++
      Fragment.const(
        valueList
          .map(value => value)
          .mkString(", ")
      ) ++
      fr")"
    println(fullGoal)

    fullGoal.update.run
      .transact(transactor)
      .map(id => ())
  }

}
