package data_structures

trait Trie {
  def add(s: String): Trie
  def contains(s: String): Boolean
  def prefixesMatchingString(s: String): Set[String]
  def stringsMatchingPrefix(s: String): Set[String]
}

case class Node(hasValue: Boolean, children: Map[Char, Node] = Map()) {
  def add(s: Seq[Char]): Node = {
    println("s: " + s)
    s match {
      case nextChar +: Seq() =>
        children.get(nextChar) match {
          case Some(childNode) => this.copy(children = children.updated(nextChar, childNode.copy(hasValue = true)))
          case None => this.copy(children = children.updated(nextChar, Node(true)))
        }
      case nextChar +: restOfString =>
        children.get(nextChar) match {
          case Some(childNode) =>
            this.copy(children = children.updated(nextChar, childNode.add(restOfString)))
          case None => this.copy(children = children.updated(nextChar, Node(false).add(restOfString)))
        }
    }
  }

  def contains(s: Seq[Char]): Boolean = s match {
    case nextChar +: Seq() => {
      children.get(nextChar).map(_.hasValue).getOrElse(false)

    }
    case nextChar +: restOfWord => {
      ???
    }

  }
}


case class TrieImpl(root: Node = Node(false)) extends Trie {

  override def add(s: String): TrieImpl = {
    TrieImpl(
      root.add(s)
    )
  }

  override def contains(s: String): Boolean = ???

  override def prefixesMatchingString(s: String): Set[String] = ???

  override def stringsMatchingPrefix(s: String): Set[String] = ???
}

object Trie {
  def return1() = 1

}
