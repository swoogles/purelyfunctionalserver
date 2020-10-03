package data_structures

import utest._

object TrieTest extends TestSuite {
  val tests = Tests {
    test("first_test", {
      val trie = TrieImpl()
//        .add("a")
//        .add("ab")
      .add("mango")
      .add("mandarin")
      .add("map")
      .add("man")
//        .add("abc")
      pprint.pprintln(trie)
//      Set("mango","mandarin","map","man").contains("map") ==> true
//      Trie.return1() ==> 1
//      ""
    })
  }
}
