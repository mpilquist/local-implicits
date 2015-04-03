package com.github.mpilquist.locals

import scala.tools.nsc
import nsc.Global
import nsc.Phase
import nsc.plugins.Plugin
import nsc.plugins.PluginComponent
import nsc.transform.Transform
import nsc.transform.InfoTransform
import nsc.transform.TypingTransformers
import nsc.symtab.Flags._
import nsc.ast.TreeDSL
import nsc.typechecker

class LocalImplicitsPlugin(val global: Global) extends Plugin {
  val name = "local-implicits"
  val description = "Introduces syntax for locally scoped implicits"
  val components = new LocalImplicitsTransform(this, global) :: Nil
}

class LocalImplicitsTransform(plugin: Plugin, val global: Global) extends PluginComponent with Transform with TreeDSL {
  import global._

  val runsAfter = "parser" :: Nil
  val phaseName = "local-implicit-scoper"

  val WithImplicitName = TermName("withImplicit")
  val WithImplicitsName = TermName("withImplicits")

  def newTransformer(unit: CompilationUnit) = new Transformer() {
    override def transform(tree: Tree): Tree = {
      tree match {
        case Apply(Apply(Ident(WithImplicitName | WithImplicitsName), implicits), blocks) =>
          if (blocks.size == 1) super.transform(expandImplicits(implicits, blocks.head))
          else super.transform(tree)

        case other => super.transform(other)
      }
    }
  }

  private def expandImplicits(implicits: List[Tree], block: Tree): Tree = {
    val implicitVals = implicits.zipWithIndex map { case (imp, idx) =>
      ValDef(Modifiers(IMPLICIT), TermName("local$" + idx), TypeTree(), imp)
    }
    Block(implicitVals, block)
  }
}
