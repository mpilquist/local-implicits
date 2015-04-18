# Local Implicits

This project contains a Scala compiler plugin which adds support for locally scoped implicit values.

For example:

```scala
imply("Hello, world!") { implicitly[String] }
```

expands to:

```scala
{
  implicit val implied$0 = "Hello, world!"
  implicitly[String]
}
```

which at runtime, evaluates to `"Hello, world!"`.

The `imply` method takes two parameter lists -- the first parameter list contains 1 or more values to declare in a local implicit scope, and the second parameter list contains a block to evaluate in context of the local implicit scope.

## Motivation

Defining implicits in local scope with vanilla Scala is problematic for a number of reasons.

 - In some cases, opening a new block requires either a semi-colon on the line before the start
   of the block, or an empty line before the start of the block.
 - Arbitrary names have to be assigned to each implicit.
 - The resulting code is rather verbose considering its function.

The `imply` method is particularly useful when working with type class instances that have multiple lawful implementations. For example, consider some monoid instances for `Int`:

```scala
2 |+| 3                              // 5
imply(intMultiplication) { 2 |+| 3 } // 6
imply(minMonoid) { 2 |+| 3 }         // 2
imply(maxMonoid) { 2 |+| 3 }         // 3
```

## Usage

This plugin currently supports Scala 2.10 and 2.11.

To use this plugin with SBT, add the following to build.sbt:

```scala
addCompilerPlugin("com.github.mpilquist" %% "local-implicits" % "0.3.0")
```

