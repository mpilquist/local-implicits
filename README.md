# Local Implicits

This project contains a Scala compiler plugin which adds support for locally scoped implicit values.

For example:

```scala
withImplicit("Hello, world!") { implicitly[String] }
```

expands to:

```scala
{
  implicit val local$1 = "Hello, world!"
  implicitly[String]
}
```

which at runtime, evaluates to `"Hello, world!"`.

The `withImplicit` and `withImplicits` methods each take two parameter lists -- the first parameter list contains 1 or more values to declare in a local implicit scope, and the second parameter list contains a block to evaluate in context of the local implicit scope.

## Motivation

Defining implicits in local scope with vanilla Scala is problematic for a number of reasons.

 - In some cases, opening a new block requires either a semi-colon on the line before the start
   of the block, or an empty line before the start of the block.
 - Arbitrary names have to be assigned to each implicit.
 - The resulting code is rather verbose considering its function.

The `withImplicit` and `withImplicits` methods are particularly useful when working with type class instances that have multiple lawful implementations. For example, consider some monoid instances for `Int`:

```scala
1 |+| 2                                     // 3
withImplicit(intMultiplication) { 1 |+| 2 } // 2
withImplicit(minMonoid) { 1 |+| 2 }         // 1
withImplicit(maxMonoid) { 1 |+| 2 }         // 2
```

## Usage

This plugin currently supports Scala 2.10 and 2.11.

To use this plugin with SBT, add the following to build.sbt:

```scala
addCompilerPlugin("com.github.mpilquist" %% "local-implicits" % "0.1.0")
```

