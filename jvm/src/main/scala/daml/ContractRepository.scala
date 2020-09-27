package daml

import doobie.util.transactor.Transactor
import zio.Task
import doobie.implicits._
import zio.interop.catz._

trait ContractRepository {
  def insert[T](contract: Contract[T]): Task[Unit]

}

class ContractRepositoryImpl(transactor: Transactor[Task]) extends ContractRepository {
  override def insert[T](contract: Contract[T]): Task[Unit] = {
    sql"""
         |INSERT INTO ${contract.value.name} (
         |  contract_id
         |) VALUES (
         |  ${contract.contractId.uuid.toString}
         |  )""".stripMargin.update
      .run
//      .withUniqueGeneratedKeys[Long]("id")
      .transact(transactor)
      .map(id => ())
  }
}
