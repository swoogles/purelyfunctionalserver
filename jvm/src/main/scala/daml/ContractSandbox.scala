package daml

import daml.ApiTypes.Party

trait ContractSandbox {

  trait db {
    def save[T](template: Template): Unit
    def save[T](contract: Contract): Unit
    def save[T](party: Party): Unit
    def get[T](partyName: String): Party
  }
}
