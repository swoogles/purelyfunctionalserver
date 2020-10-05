package data_structures

import utest._

object TrieTest /* UNDO extends TestSuite */ {
  val tests = Tests {
    val trie = TrieImpl()
      .add("mango")
      .add("mandarin")
      .add("map")
      .add("man")
    test("contains", {
        trie
        .contains("mango") ==> true
    })
    test("prefixesMatchingString", {
      trie
        .prefixesMatchingString("mangosteen") ==> Set("man", "mango")

    })
    test("stringsMatchingPrefixes", {
        trie
          .stringsMatchingPrefix("ma") ==> Set("mango", "mandarin", "map", "man")
    })
  }
}
