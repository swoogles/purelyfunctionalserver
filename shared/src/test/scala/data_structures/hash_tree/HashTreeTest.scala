package data_structures.hash_tree

import utest.{TestSuite, Tests, test}

object HashTreeTest extends TestSuite {
  val tests = Tests {
    test("contains", {

      pprint.pprintln(Node(List(
        Leaf(DataBlock("b1")),
        Leaf(DataBlock("b2")),
      )))
    })
  }
}
