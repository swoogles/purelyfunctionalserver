package data_structures.hash_tree

import java.time.Instant

case class DataBlock(rawContent: String)

case class Hash(value: String)

object Hash {

  def of(dataBlock: DataBlock): Hash =
    Hash(dataBlock.rawContent + "#")

  def of(transaction: Transaction): Hash =
    Hash("#" + transaction.acceptor.name + "->" + transaction.giver.name + "#")

  def of(header: Header): Hash =
    Hash("BLOCK:" + header.merkleRootHash)
}

trait HashTreeElement {
  val hash: Hash

}

// todo remove case
case class Leaf(dataBlock: DataBlock) extends HashTreeElement {

  val hash =
    Hash.of(dataBlock)
}

case class TransactionLeaf(transaction: Transaction, hash: Hash) extends HashTreeElement {}

object TransactionLeaf {

  def apply(transaction: Transaction): TransactionLeaf =
    TransactionLeaf(transaction, Hash.of(transaction))
}

// todo remove case
case class Node(hash: Hash, children: Seq[HashTreeElement]) extends HashTreeElement {}

object Node {

  def apply(children: List[HashTreeElement]): Node =
    Node(
      Hash(children.map(_.hash.value).mkString(":")),
      children
    )
}

object MerkleTree {

  def apply(rawData: List[DataBlock]): HashTreeElement =
    rawData match {
      case head :: tail =>
        tail.foldLeft[HashTreeElement](Leaf(head))(
          (acc, nextBlock) => Node(List(acc, Leaf(nextBlock)))
        )
      case Nil => Node(Hash("EMPTY ROOT HASH"), Seq())
    }

  def ofTransactions(rawData: List[Transaction]): HashTreeElement =
    rawData match {
      case head :: tail =>
        tail.foldLeft[HashTreeElement](TransactionLeaf(head))(
          (acc, nextBlock) => Node(List(acc, TransactionLeaf(nextBlock)))
        )
      case Nil => Node(Hash("EMPTY ROOT HASH"), Seq())
    }

  // todo dunno that this is actually important
  def applyInPasses(transactions: List[Transaction]): Hash =
    applyToNodesInPasses(
      transactions
        .grouped(2)
        .map {
          case item1 :: item2 :: Nil => Node(List(TransactionLeaf(item1), TransactionLeaf(item2)))
          case item1 :: Nil          => Node(List(TransactionLeaf(item1), TransactionLeaf(item1)))
          case Nil                   => throw new RuntimeException("not handled")
        }
        .toList
    )

  def applyToNodesInPasses(transactions: List[Node]): Hash =
    if (transactions.length == 1)
      transactions.head.hash
    else {
      applyToNodesInPasses(
        transactions
          .grouped(2)
          .map {
            case item1 :: item2 :: Nil => Node(List(item1, item2))
            case item1 :: Nil          => Node(List(item1, item1))
            case Nil                   => throw new RuntimeException("not handled")
          }
          .toList
      )
    }

}

trait User {
  def submitTransaction(dataBlock: DataBlock, lastKnownState: HashTreeElement): HashTreeElement
}

trait BlockChain {
  def blocks: List[DataBlock]
  def addBlock(dataBlock: DataBlock): BlockChain
}

case class Block(
  header: Header,
  transactions: List[Transaction]
)

object BlockChain {

  def newChain(timestamp: Instant, transactions: List[Transaction]): Block =
    Block(Header(None, MerkleTree.ofTransactions(transactions).hash, timestamp), transactions)

  def linkedTo(previous: Block, timestamp: Instant, transactions: List[Transaction]) =
    Block(Header(Some(Hash.of(previous.header)),
                 MerkleTree.ofTransactions(transactions).hash,
                 timestamp),
          transactions)

}

case class Header(
  previousBlockHash: Option[Hash],
  merkleRootHash: Hash,
  timestamp: Instant
)

case class Party(name: String)

case class Transaction(
  acceptor: Party,
  giver: Party
)
