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

    "not support overriding an implicit declared in the same scope as the expression" in {
      """
      implicit val x = 1
      imply(42) { implicitly[Int] }
      """ shouldNot compile
    }
  }
}
