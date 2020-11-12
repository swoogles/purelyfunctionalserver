package data_structures.hash_tree

import java.time.Instant

import utest.{TestSuite, Tests, test}

object HashTreeTest extends TestSuite {
  val tests = Tests {
    test("contains", {

//      pprint.pprintln(Node(List(
//        Leaf(DataBlock("b1")),
//        Leaf(DataBlock("b2")),
//      )))
    })

//    test("blockChain", {
//      pprint.pprintln(
//        MerkleTree.apply(List(DataBlock("b1"), DataBlock("b2"), DataBlock("b3")))
//      )
//    })

    test("transactionHashes", {
      pprint.pprintln(
        MerkleTree.ofTransactions(List(Transaction(Party("A"), Party("B")))).hash
      )
    })

    test("chain creation", {
      val firstBlock =
        BlockChain.newChain(Instant.now(), List(Transaction(Party("A"), Party("B"))))
      pprint.pprintln(
        BlockChain.linkedTo(firstBlock,
          Instant.now().plusSeconds(3600), List(Transaction(Party("C"), Party("D")))
        )
      )
    })

    test({
      pprint.pprintln(
        MerkleTree.ofTransactions(List(Transaction(Party("A"), Party("B")), Transaction(Party("C"), Party("D")), Transaction(Party("E"), Party("F"))))
      )
    })

  }
}
//