package data_structures.hash_tree

import java.time.Instant

case class DataBlock(rawContent: String)

case class Hash(value: String)

case class CompositeHash(hashes: Set[Hash]) {
//  hashes.
}

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
//case class Leaf(dataBlock: DataBlock) extends HashTreeElement {
//  val hash =
//    Hash.of(dataBlock)
//}

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

  def ofTransactions(rawData: List[Transaction]): HashTreeElement =
    rawData match {
      case head :: tail =>
        tail.foldLeft[HashTreeElement](TransactionLeaf(head))(
          (acc, nextBlock) => Node(List(acc, TransactionLeaf(nextBlock)))
        )
      case Nil => Node(Hash("EMPTY ROOT HASH"), Seq())
    }

  // todo dunno that this is actually important
  def applyInPasses(transactions: List[Transaction]): HashTreeElement =
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

  def applyToNodesInPasses(nodes: List[Node]): HashTreeElement =
    if (nodes.length == 1)
      nodes.head
    else {
      applyToNodesInPasses(
        nodes
          .grouped(2)
          .map {
            case item1 :: item2 :: Nil => Node(List(item1, item2))
            case item1 :: Nil          => Node(List(item1, item1))
            case Nil                   => throw new RuntimeException("not handled")
          }
          .toList
      )
    }

  def merklePathOf(transaction: Transaction, hashTreeElement: HashTreeElement): Seq[Hash] =
    hashTreeElement match {
      case Node(hash, children) =>
        if (hash.value.contains(transaction.giver.name) || hash.value.contains(transaction.acceptor.name))
          children.map(merklePathOf(transaction, _)).reduce(_ ++ _)
        else
          Seq(hash)
//          children.map(merklePathOf(transaction, _)).reduce(_ ++ _)

      case TransactionLeaf(leafTransaction, hash) =>
        if (transaction == leafTransaction)
          Seq()
        else
          Seq(hash)

    }

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
