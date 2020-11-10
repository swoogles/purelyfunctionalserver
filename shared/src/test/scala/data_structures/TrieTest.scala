package data_structures

import utest._

object TrieTest extends TestSuite {
  val tests = Tests {
    val trie = TrieImpl()
      .add("mango")
      .add("mandarin")
      .add("map")
      .add("man")
      .add("dan")
      .add("danger")
      .add("damnit")
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
    test("stringsMatchingPrefixes allStrings", {
      trie
        .stringsMatchingPrefix("") ==> Set("mango", "mandarin", "map", "man", "dan", "danger", "damnit")
    })
    test("stringsMatchingPrefixes allStrings", {
      val trie = TrieImpl()
        .add("me")
        .add("men")
      pprint.pprintln(trie.delete("me").delete("men"))
    })
  }
}
