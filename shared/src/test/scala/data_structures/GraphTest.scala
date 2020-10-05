package data_structures

import utest._

import scala.collection.immutable.HashMap

object GraphTest /* UNDO extends TestSuite*/ {
  val cyclicGraph =
  new CyclicGraph(
    HashMap(
      "a" -> Seq("b", "c"),
      "b" -> Seq("a"),
      "c" -> Seq("b"))
  )
  val tests = Tests {
    test("search", {
      pprint.pprintln(cyclicGraph.breadthFirstSearch("c", Set()))
    })
    val dag =
      new CyclicGraph(
        HashMap(
          "a"-> Seq("b", "d", "f"),
          "b"-> Seq("c"),
          "c"-> Seq("e"),
          "d"-> Seq("e"),
          "e"-> Seq(),
          "f" -> Seq("g"),
          "g" -> Seq("h"),
        )
      )
    test("search", {
      pprint.pprintln(dag.breadthFirstSearch("c", Set()))
    })
    test("Shortest Path", {
      pprint.pprintln(dag.shortestPaths("a","h", HashMap(), List()))
    })
  }
}
