package data_structures

import utest._

object GraphTest extends TestSuite {
  val cyclicGraph =
  new CyclicGraph(
    Map(
      "a" -> Seq("b", "c"),
      "b" -> Seq("a"),
      "c" -> Seq("b"))
  )
  val tests = Tests {
    test("search", {
      pprint.pprintln(cyclicGraph.breadthFirstSearch("c", Set()))
    })
  }
}
