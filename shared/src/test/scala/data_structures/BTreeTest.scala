package data_structures

import utest._

import scala.collection.immutable.HashMap

object BTreeTest extends TestSuite {
  val bTree =
    new BTree(LeafNode, 3)
  val tests = Tests {
    test("insert", {
      pprint.pprintln(
        bTree.insert(2)
          .insert(8)
          .insert(7)
          .insert(3)
          .insert(4)
          .insert(10)
          .insert(12)
          .insert(14)
          .insert(15)
      )
    })
  }
}
