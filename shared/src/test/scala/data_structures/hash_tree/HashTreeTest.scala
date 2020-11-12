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

    test({
      pprint.pprintln(
        MerkleTree.ofTransactions(List(Transaction(Party("A"), Party("B")), Transaction(Party("C"), Party("D")), Transaction(Party("E"), Party("F"))))
      )
    })

    test({
      val merkleTree =
        MerkleTree.applyInPasses(
          List(
            Transaction(Party("A"), Party("B")),
            Transaction(Party("C"), Party("D")),
            Transaction(Party("E"), Party("F")),
            Transaction(Party("G"), Party("H")),
            Transaction(Party("I"), Party("J")),
            Transaction(Party("K"), Party("L")),
          ))

      pprint.pprintln("MerkleHash to recreate: " + merkleTree.hash)

      val transaction = Transaction(Party("A"), Party("B"))

      val merklePath =
      MerkleTree.merklePathOf(
        transaction,
        merkleTree
      )
      pprint.pprintln( merklePath )

      pprint.pprintln("Reconstructed hash:" +
        merklePath.foldLeft(Hash.of(transaction)){ case (hashSoFar: Hash, nextHash: Hash) => Hash(Seq(hashSoFar, nextHash))}
      )

    })

  }
}
//