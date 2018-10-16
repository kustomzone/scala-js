/*
 * Scala.js (https://www.scala-js.org/)
 *
 * Copyright EPFL.
 *
 * Licensed under Apache License 2.0
 * (https://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package org.scalajs.nscplugin.test

import util._

import org.junit.Test

import org.scalajs.ir.{Trees => js, Types => jstpe}

class OptimizationTest extends JSASTTest {
  import OptimizationTest._

  @Test
  def testJSArrayApplyOptimization: Unit = {
    /* Make sure js.Array(...) is optimized away completely for several kinds
     * of data types.
     */
    """
    import scala.scalajs.js

    class VC(val x: Int) extends AnyVal

    class A {
      val a = js.Array(5, 7, 9, -3)
      val b = js.Array("hello", "world")
      val c = js.Array('a', 'b')
      val d = js.Array(Nil)
      val e = js.Array(new VC(151189))
    }
    """.
    hasNot("any of the wrapArray methods") {
      case WrapArrayCall() =>
    }
  }

  @Test
  def testVarArgsOptimization: Unit = {
    /* Make sure varargs are optimized to use js.WrappedArray instead of
     * scm.WrappedArray, for various data types.
     */
    """
    import scala.scalajs.js

    class VC(val x: Int) extends AnyVal

    class A {
      val a = List(5, 7, 9, -3)
      val b = List("hello", "world")
      val c = List('a', 'b')
      val d = List(Nil)
      val e = List(new VC(151189))
    }
    """.
    hasNot("any of the wrapArray methods") {
      case WrapArrayCall() =>
    }

    /* #2265 and #2741:
     * Make sure varargs are optimized to use js.WrappedArray instead of
     * scm.WrappedArray, for different species of target method (single arg
     * list, multiple arg list, in value class).
     */
    """
    import scala.scalajs.js

    class VC(val x: Int) extends AnyVal {
      def singleInVC(ys: Int*): Int = x + ys.size
    }

    class A {
      def test(): Int = {
        val a = single(5, 7, 9, -3)
        val b = multiple(5)(7, 9, -3)
        val c = new VC(5).singleInVC(7, 9, -3)
        a + b + c
      }

      def single(x: Int, ys: Int*): Int = x + ys.size
      def multiple(x: Int)(ys: Int*): Int = x + ys.size
    }
    """.
    hasNot("any of the wrapArray methods") {
      case WrapArrayCall() =>
    }

    /* Make sure our wrapper matcher has the right name.
     * With the new collections, only actual varargs will produce a call to the
     * methods we optimize, and we would always be able to optimize them in
     * that case. So we need to explicitly call the method that the codegen
     * would use.
     */
    val sanityCheckCode = if (hasOldCollections) {
      """
      class A {
        val a: Seq[Int] = new Array[Int](5)
      }
      """
    } else {
      """
      class A {
        runtime.ScalaRunTime.wrapIntArray(new Array[Int](5))
      }
      """
    }
    sanityCheckCode.has("one of the wrapArray methods") {
      case WrapArrayCall() =>
    }
  }

  @Test
  def testNewJSObjectAndJSArray: Unit = {
    // Verify the optimized emitted code for 'new js.Object' and 'new js.Array'
    """
    import scala.scalajs.js

    class A {
      val o = new js.Object
      val a = new js.Array
    }
    """.
    hasNot("any reference to the global scope") {
      case js.JSLinkingInfo() =>
    }
  }

  @Test
  def switchWithoutGuards: Unit = {
    """
    class Test {
      def switchWithGuardsStat(x: Int, y: Int): Unit = {
        x match {
          case 1            => println("one")
          case 2            => println("two")
          case z if y > 100 => println("big " + z)
          case _            => println("None of those")
        }
      }
    }
    """.hasNot("Labeled block") {
      case js.Labeled(_, _, _) =>
    }.has("Match node") {
      case js.Match(_, _, _) =>
    }
  }

  @Test
  def switchWithGuards: Unit = {
    // Statement position
    """
    class Test {
      def switchWithGuardsStat(x: Int, y: Int): Unit = {
        x match {
          case 1            => println("one")
          case 2 if y < 10  => println("two special")
          case 2            => println("two")
          case 3 if y < 10  => println("three special")
          case 3 if y > 100 => println("three big special")
          case z if y > 100 => println("big " + z)
          case _            => println("None of those")
        }
      }
    }
    """.hasExactly(1, "default case (\"None of those\")") {
      case js.StringLiteral("None of those") =>
    }.has("Match node") {
      case js.Match(_, _, _) =>
    }

    // Expression position
    """
    class Test {
      def switchWithGuardsExpr(x: Int, y: Int): Unit = {
        val message = x match {
          case 1            => "one"
          case 2 if y < 10  => "two special"
          case 2            => "two"
          case 3 if y < 10  => "three special"
          case 3 if y > 100 => "three big special"
          case z if y > 100 => "big " + z
          case _            => "None of those"
        }
        println(message)
      }
    }
    """.hasExactly(1, "default case (\"None of those\")") {
      case js.StringLiteral("None of those") =>
    }.has("Match node") {
      case js.Match(_, _, _) =>
    }
  }

  @Test
  def newSJSDefinedTraitProducesObjectConstr: Unit = {
    """
    import scala.scalajs.js
    import scala.scalajs.js.annotation._

    trait Point extends js.Object {
      val x: Double
      val y: Double
    }

    class Test {
      def newSJSDefinedTraitProducesObjectConstr(): Any = {
        new Point {
          val x = 5.0
          val y = 6.5
        }
      }
    }
    """.hasNot("`new Object`") {
      case js.JSNew(_, _) =>
    }.has("object literal") {
      case js.JSObjectConstr(Nil) =>
    }

    """
    import scala.scalajs.js
    import scala.scalajs.js.annotation._

    trait Point extends js.Object {
      var x: js.UndefOr[Double] = js.undefined
      var y: js.UndefOr[Double] = js.undefined
    }

    class Test {
      def newSJSDefinedTraitProducesObjectConstr(): Any = {
        new Point {
          x = 5.0
          y = 6.5
        }
      }
    }
    """.hasNot("`new Object`") {
      case js.JSNew(_, _) =>
    }.has("object literal") {
      case js.JSObjectConstr(Nil) =>
    }
  }

}

object OptimizationTest {

  private val hasOldCollections = {
    val version = scala.util.Properties.versionNumberString

    version.startsWith("2.10.") ||
    version.startsWith("2.11.") ||
    version.startsWith("2.12.") ||
    version == "2.13.0-M3"
  }

  private object WrapArrayCall {
    private val Suffix =
      if (hasOldCollections) "__scm_WrappedArray"
      else "__sci_ArraySeq"

    def unapply(tree: js.Apply): Boolean = {
      val methodName = tree.method.name
      methodName.startsWith("wrap") && methodName.endsWith(Suffix)
    }
  }

}
