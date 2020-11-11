package data_structures.hash_tree

case class DataBlock(rawContent: String)

case class Hash(value: String)

object Hash {

  def of(dataBlock: DataBlock): Hash =
    Hash(dataBlock.rawContent + "#")
}

trait HashTreeElement {
  val hash: Hash

}

// todo remove case
case class Leaf(dataBlock: DataBlock) extends HashTreeElement {

  val hash =
    Hash.of(dataBlock)
}

// todo remove case
case class Node(hash: Hash) extends HashTreeElement {}

object Node {

  def apply(children: List[Leaf]): Node =
    Node(
      Hash(children.map(_.hash).mkString(":"))
    )
}

class MerkleTree {}
