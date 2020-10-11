package data_structures

import scala.collection.immutable.HashMap

class Graph {}

class CyclicGraph(nodes: HashMap[String, Seq[String]]) {

  def breadthFirstSearch(startingPoint: String, seen: Set[String]): Set[String] = {
    val updatedSeen = seen + startingPoint
    nodes.get(startingPoint) match {
      case Some(neighbors) =>
        neighbors
          .map(
            neighbor =>
              if (seen.contains(neighbor))
                updatedSeen
              else
                breadthFirstSearch(neighbor, updatedSeen)
          )
          .foldLeft(updatedSeen)(_ ++ _)
      case None => updatedSeen
    }
  }

  def shortestPaths(startingPoint: String,
                    endingPoint: String,
                    shortestPathsSoFar: HashMap[String, List[String]],
                    currentPath: List[String]): HashMap[String, List[String]] =
//    def shortestPath(startingPoint: String, endingPoint: String, pathSoFar: List[String]): Map[String, List[String]] = {
    if (startingPoint == endingPoint) {
      if (currentPath.length < shortestPathsSoFar
            .get(endingPoint)
            .map(_.length)
            .getOrElse(Int.MaxValue)) {
        shortestPathsSoFar.updated(endingPoint, currentPath :+ endingPoint)
      } else {
        shortestPathsSoFar
      }
    } else {
      if (shortestPathsSoFar.contains(startingPoint)) {
        shortestPathsSoFar
      } else {
        nodes.get(startingPoint) match {
          case Some(neighbors) =>
            neighbors
              .map(
                neighbor =>
                  shortestPaths(neighbor,
                                endingPoint,
                                shortestPathsSoFar.updated(startingPoint,
                                                           currentPath :+ startingPoint),
                                currentPath :+ startingPoint)
              )
              .foldLeft(shortestPathsSoFar) {
                case (map1, map2) =>
                  map1.merged(map2) {
                    case ((key1, value1), (key2, value2)) =>
                      (key1, if (value1.length < value2.length) value1 else value2)
                  }
              }
          case None => HashMap()
        }

      }
    }

}
