package data_structures

import utest._

object TrieTest extends TestSuite {
  val tests = Tests {
    val trie = TrieImpl()
      .add("mango")
      .add("mandarin")
      .add("map")
      .add("man")
    test("first_test", {
      //        .add("abc")
      pprint.pprintln(trie)
      //      Set("mango","mandarin","map","man").contains("map") ==> true
      //      Trie.return1() ==> 1
      //      ""
    })
    test("contains", {
        trie
        .contains("mango") ==> true
    })
    test("prefixesMatchingString", {
      pprint.pprintln(
      trie
        .prefixesMatchingString("mangosteen")
      )
    })
  }
}
