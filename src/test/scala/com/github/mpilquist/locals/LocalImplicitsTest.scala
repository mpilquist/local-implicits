package com.github.mpilquist.locals

import org.scalatest.{ WordSpec, Matchers }

class LocalImplicitsTest extends WordSpec with Matchers {

  "the local method" should {
    "evaluate the second parameter list with each value in the first parameter list declared implicitly" in {
      val result = imply(42) { implicitly[Int] }
      result shouldBe 42
    }

    "support multiple implicits" in {
      val result = imply(42, "hi") { (implicitly[Int], implicitly[String]) }
      result shouldBe (42, "hi")
    }

    "allow support simple syntax for selecting non-default type class instances" in {
      trait Semigroup[A] { def combine(x: A, y: A): A }
      object Semigroup {
        implicit val intAddition: Semigroup[Int] = new Semigroup[Int] { def combine(x: Int, y: Int) = x + y }
         val intMultiplication: Semigroup[Int] = new Semigroup[Int] { def combine(x: Int, y: Int) = x * y }
      }
      implicit class SemigroupOps[A](val lhs: A)(implicit sg: Semigroup[A]) {
        def |+|(rhs: A): A = sg.combine(lhs, rhs)
      }

      (2 |+| 5) shouldBe 7
      imply(Semigroup.intMultiplication) { (2 |+| 5) } shouldBe 10
    }

    "support overriding an implicit declared in the same scope as the expression" in {

      {
        implicit val x = 1
        imply(42) { implicitly[Int] } shouldBe 42
      }

      object Foo {
        implicit val x = 1
        imply(42) { implicitly[Int] }
      }
    }

    "support implying a more-specific type than is needed in the block, while the needed type is available in the same scope" in {
      trait Semigroup[A] { def combine(x: A, y: A): A }
      trait Monoid[A] extends Semigroup[A] { def id: A }

      implicit val add = new Semigroup[Int] {
        def combine(x: Int, y: Int) = x + y
      }

      val mult = new Monoid[Int] {
        def combine(x: Int, y: Int) = x * y
        def id = 1
      }

      imply(mult) { implicitly[Monoid[Int]].id } shouldBe 1
      imply(mult) { implicitly[Semigroup[Int]].combine(1, 2) } shouldBe 2
    }
  }
}
