package daml

import java.util.UUID

import daml.rpcvalue.Record

object rpcvalue {
  case class Value(arguments: Seq[String])
  case class Record(arguments: Seq[String])
}

case class ContractId[+T] (uuid: UUID)
case class Template[+T] (name: String, arguments: Seq[String],
                         id: String,
//val consumingChoices: Set[Choice]
                        )

final case class Contract[+T](
                               contractId: ContractId[T],
                               value: T with Template[T],
                               agreementText: Option[String],
                               signatories: Seq[String],
                               observers: Seq[String],
                               key: Option[rpcvalue.Value]) {
  def arguments: rpcvalue.Record = Record(value.arguments)
}