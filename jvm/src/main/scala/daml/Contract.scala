package daml

import java.util.UUID

import daml.ApiTypes.Party
import daml.rpcvalue.Record
import doobie.Fragment
import doobie._
import doobie.implicits._

object rpcvalue {
  case class Value(arguments: Seq[String])
  case class Record(arguments: Seq[UserDefinedColumn])
}

sealed trait TemplateColumnType
case class TextColumn(name: String) extends TemplateColumnType
case class PartyColumn(name: String) extends TemplateColumnType
case class DateColumn(name: String) extends TemplateColumnType

object TemplateColumnType {

  def convertToSqlColumnDefinition(templateColumnType: TemplateColumnType) =
    templateColumnType match {
      case TextColumn(name)  => s" $name TEXT"
      case PartyColumn(name) => s" $name TEXT"
      case DateColumn(name)  => s" $name DATE"
    }
}

object ColumnConversions {

  def convertParty(party: Party) = // todo figure out tagged type issue that prevents circe auto-derivation
    s"$party TEXT"

  def convertParty(party: String) =
    s"$party TEXT"

}

case class UserDefinedColumn(name: String, columnType: String)
case class UserDefinedColumnValue(name: String, value: String)

object UserDefinedColumn {

  def convertToSqlColumnDefinition(userDefinedColumn: UserDefinedColumn) = userDefinedColumn match {
    case UserDefinedColumn(name, columnType) if columnType == "TEXT"  => s" $name TEXT"
    case UserDefinedColumn(name, columnType) if columnType == "PARTY" => s" $name TEXT"
    case UserDefinedColumn(name, columnType) if columnType == "DATE"  => s" $name DATE"
  }

  def convertToSqlColumnDefinitions(userDefinedColumns: Seq[UserDefinedColumn]) =
    Fragment.const(userDefinedColumns.map(convertToSqlColumnDefinition).mkString(","))

}

case class ContractId[+T](uuid: UUID)

case class Template(name: String,
                    arguments: Seq[UserDefinedColumn],
                    signatories: Seq[String]
//val consumingChoices: Set[Choice]
)

final case class Contract(
  contractId: Int,
  template: Template,
  agreementText: String,
  userDefinedColumnValues: Seq[UserDefinedColumnValue],
  signatories: Seq[String]
) {}
