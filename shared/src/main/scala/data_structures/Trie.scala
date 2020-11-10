package data_structures

import scala.collection.immutable.{Map, Set}

trait Trie {
  def add(s: String): Trie
  def delete(s: String): Trie
  def contains(s: String): Boolean
  def prefixesMatchingString(s: String): Set[String]
  def stringsMatchingPrefix(s: String): Set[String]
}

case class TrieNode(hasValue: Boolean, children: Map[Char, TrieNode] = Map()) {

  def add(s: Seq[Char]): TrieNode =
    s match {
      case Seq() => this.copy(hasValue = true)

      case nextChar +: restOfString =>
        this.copy(
          children =
            children.updated(nextChar,
                             children.getOrElse(nextChar, TrieNode(false)).add(restOfString))
        )

    }

  def contains(s: Seq[Char]): Boolean = s match {
    case Seq() => true
    case nextChar +: restOfWord =>
      children
        .get(nextChar)
        .exists(_.contains(restOfWord))

  }

  def stringsDownToThisLevel(charsSoFar: Seq[Char]): Set[String] =
    if (hasValue)
      Set(charsSoFar.mkString)
    else Set()

  def prefixesMatchingString(s: Seq[Char], charsSoFar: Seq[Char]): Set[String] =
    stringsDownToThisLevel(charsSoFar) ++ (s match {
      case Seq() => Set()

      case nextChar +: restOfWord =>
        children.get(nextChar) match {
          case Some(child) => child.prefixesMatchingString(restOfWord, charsSoFar :+ nextChar)
          case None        => Set()
        }
    })

  def allValuesBeneathThisPoint(charsSoFar: Seq[Char]): Set[String] =
    children
      .map { case (key, child) => child.allValuesBeneathThisPoint(charsSoFar :+ key) }
      .foldLeft(stringsDownToThisLevel(charsSoFar))(_ ++ _)
      .filter(_.nonEmpty)

  val allValuesStrictlyBeneathThisPoint: Set[String] =
    children
      .map { case (key, child) => child.allValuesStrictlyBeneathThisPoint }
      .foldLeft(Set[String]())(_ ++ _)

  def stringsMatchingPrefix(s: Seq[Char], charsSoFar: Seq[Char]): Set[String] =
    s match {
      // We've fully consumed the input String, so everything below this point is a match
      case Seq() => allValuesBeneathThisPoint(charsSoFar)
      // We need to keep eating characters before we can decide what the matches are
      case nextChar +: restOfWord =>
        children.get(nextChar) match {
          case Some(child) => child.stringsMatchingPrefix(restOfWord, charsSoFar :+ nextChar)
          case None        => Set()
        }
    }

  def delete(input: Seq[Char]): Option[TrieNode] =
    input match {
      // We've fully consumed the input String, so everything below this point is a match
      case Seq() => {
        // children.map{ case (char, node) => }
        println("all values beneath:")
        allValuesBeneathThisPoint(input) match {
          case isEmpty => None
          case allValuesBeneath =>
            Some(
              allValuesBeneath
                .foldLeft(this.copy(hasValue = false, children = Map())) {
                  case (acc, nextValue) => acc.add(nextValue)
                }
            )
        }
      }
      // We need to keep eating characters before we can decide what the matches are
      case nextChar +: restOfWord => {
        println("Recursing with word remainder: " + restOfWord)
        Some(
          this.copy(
            children = children
              .map {
                case (char, child) =>
                  if (char == nextChar)
                    (char, child.delete(restOfWord))
                  else
                    (char, Some(child))
              }
              .flatMap { case (char, child) => child.map { (char, _) } }
              .toMap
          )
        )
//        val unaffectedChildren: Map[Char, TrieNode] = children.removed(nextChar)
//        val updatedAffectedChild: Option[TrieNode] = children.get(nextChar).flatMap { (child) =>
//          child.delete(restOfWord)
//        }
//        updatedAffectedChild.flatMap {
//          case affectedChild =>
//            Some(
//              this.copy(
//                children =
//                  unaffectedChildren + (nextChar -> affectedChild)
//              )
//            )
//        }.getOrElse(Some(this))
      }
    }

}

case class TrieImpl(root: TrieNode = TrieNode(false)) extends Trie {

  override def add(s: String): TrieImpl =
    TrieImpl(
      root.add(s)
    )

  override def contains(s: String): Boolean = root.contains(s)

  override def prefixesMatchingString(s: String): Set[String] =
    root.prefixesMatchingString(s, Seq())

  override def stringsMatchingPrefix(s: String): Set[String] = root.stringsMatchingPrefix(s, Seq())

  override def delete(s: String): Trie = TrieImpl(root.delete(s).getOrElse(TrieNode(false)))
}

object Trie {
  def return1() = 1

}
