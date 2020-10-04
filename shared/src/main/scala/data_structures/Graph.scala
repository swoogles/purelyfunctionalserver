package data_structures

class Graph {
  val dag = Map(
    "a"-> Seq("b","c"),
    "b"-> Seq("c","d"),
    "c"-> Seq("d"),
    "d"-> Seq())
}

class CyclicGraph(nodes: Map[String, Seq[String]]) {
  def breadthFirstSearch(startingPoint: String, seen: Set[String]): Set[String] = {
    nodes.get(startingPoint) match {
      case Some(neighbors) =>
        neighbors.map(neighbor =>
          if (seen.contains(neighbor))
            seen + startingPoint
          else
            breadthFirstSearch(neighbor, seen + startingPoint)
        ).reduce( _ ++ _)
    }

  }

}
