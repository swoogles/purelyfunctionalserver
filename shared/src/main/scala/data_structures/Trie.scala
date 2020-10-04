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

  def addV2(s: Seq[Char]): Node =
    s match {
      case Seq() => this.copy(hasValue = true)
      case nextChar +: restOfString =>
        children.get(nextChar) match {
          case Some(childNode) => this.copy(children = children.updated(nextChar, childNode.add(restOfString)))
          case None => this.copy(children = children.updated(nextChar, Node(false).add(restOfString)))
        }
    }

  def addV3(s: Seq[Char]): Node =
    s match {
      case Seq() => this.copy(hasValue = true)

      case nextChar +: restOfString =>
        this.copy(children =
          children.updated(
            nextChar,
            children.getOrElse(nextChar, Node(false)).add(restOfString)))

    }

  def contains(s: Seq[Char]): Boolean = s match {
    case Seq() => true
    case nextChar +: restOfWord =>
      children
        .get(nextChar)
        .exists(_.contains(restOfWord))

  }

  def prefixesMatchingString(s: Seq[Char], charsSoFar: Seq[Char]): Set[String] =
    s match {
      case Seq() =>
        if(hasValue)
          Set(charsSoFar.mkString)
        else Set()

      case nextChar +: restOfWord =>
        (children.get(nextChar) match {
          case Some(child) => child.prefixesMatchingString(restOfWord, charsSoFar :+ nextChar)
          case None => Set()
        }) ++ (
        if(hasValue)
          Set(charsSoFar.mkString)
        else Set()
          )

    }
}


case class TrieImpl(root: Node = Node(false)) extends Trie {

  override def add(s: String): TrieImpl = {
    TrieImpl(
      root.addV3(s)
    )
  }

  override def contains(s: String): Boolean = root.contains(s)

  override def prefixesMatchingString(s: String): Set[String] =  root.prefixesMatchingString(s, Seq())

  override def stringsMatchingPrefix(s: String): Set[String] = ???
}

object Trie {
  def return1() = 1

}
