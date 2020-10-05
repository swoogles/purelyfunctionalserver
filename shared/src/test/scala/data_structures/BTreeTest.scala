package data_structures

import utest._

object BTreeTest extends TestSuite {
  val bTree =
    new BTree(Leaf, 4)
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
          .insert(5)
          .insert(51)
          .insert(33)
          .insert(34)
          .insert(36)
          .insert(37)
          .insert(38)
          .insert(20)
      )
    })
  }
}
