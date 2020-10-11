package data_structures

/*
Definition

According to Knuth's definition, a B-tree of order m is a tree which satisfies the following properties:

    1. Every node has at most m children.
    2. Every non-leaf node (except root) has at least ⌈m/2⌉ child nodes.
    3. The root has at least two children if it is not a leaf node.
    4. A non-leaf node with k children contains k − 1 keys.
    5. All leaves appear in the same level and carry no information.

Each internal node's keys act as separation values which divide its subtrees.
For example, if an internal node has 3 child nodes (or subtrees) then it must have 2 keys: a1 and a2.
All values in the leftmost subtree will be less than a1, all values in the middle subtree will be
between a1 and a2, and all values in the rightmost subtree will be greater than a2.
 */
sealed trait Node {
  def insert(value: Int, maxValues: Int): Node
}

case class InternalNode(values: List[Int], children: List[Node]) extends Node {

  override def insert(value: Int, maxValues: Int): Node =
    if (values.length < maxValues) {
      val (lessThanValues, greaterThanValues) = values.partition(_ < value)
      val (lessThanChildren, greaterThanChildren) = children.splitAt(lessThanValues.length + 1)
      InternalNode(lessThanValues ::: value :: greaterThanValues,
                   lessThanChildren ::: Leaf :: greaterThanChildren)

    } else {
      val (lessThanValues, greaterThanValues) = values.partition(_ < value)
      val (lessThanChildren, targetChild :: greaterThanChildren) =
        children.splitAt(lessThanValues.length)
      InternalNode(values,
                   lessThanChildren ::: targetChild.insert(value, maxValues) :: greaterThanChildren)
    }
}

case object Leaf extends Node {

  override def insert(value: Int, maxValues: Int): Node =
    InternalNode(List(value), List(Leaf, Leaf))
}

case class BTree(root: Node, order: Int) {
  def insert(value: Int): BTree = BTree(root.insert(value, order), order)

}
