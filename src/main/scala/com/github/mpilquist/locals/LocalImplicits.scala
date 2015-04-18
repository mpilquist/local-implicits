package com.github.mpilquist.locals

import scala.language.experimental.macros
import scala.reflect.macros.whitebox.Context

object LocalImplicits {

  def implyEval(implicits: Any*)(f: String): Any = macro implyEvalMacro

  def implyEvalMacro(c: Context)(implicits: c.Expr[Any]*)(f: c.Expr[String]): c.Expr[Any] = {
    import c.universe._

    def trace(s: => String) = {
      if (sys.props.get("locals.trace").isDefined) c.info(c.enclosingPosition, s, false)
    }

    f.tree match {
      case Literal(Constant(body: String)) =>
        val imp = implicits.head
        val implicitVals = implicits.zipWithIndex map { case (imp, idx) =>
          val nme = TermName("implied$" + idx)
          q"""implicit val $nme = $imp"""
        }

        val shadowingVals = implicits flatMap { imp =>
          val existingImplicit = c.inferImplicitValue(imp.actualType.widen)
          existingImplicit match {
            case EmptyTree =>
              None
            case Ident(nme: TermName) =>
              Some(q"""val $nme = $imp""")
            case Select(This(_), nme: TermName) =>
              Some(q"""val $nme = $imp""")
            case other =>
              trace("not shadowing: " + showRaw(other))
              None
          }
        }

        if (shadowingVals.nonEmpty) trace("Shadowed implicits: " + shadowingVals.mkString("\n", "\n", ""))

        val parsedBlock = c.parse(body)
        c.Expr[Any](c.typecheck(q"""
          ..$shadowingVals
          ..$implicitVals
          $parsedBlock"""))
      case other => c.abort(c.enclosingPosition, "Macro body should be a constant string!")
    }
  }
}
