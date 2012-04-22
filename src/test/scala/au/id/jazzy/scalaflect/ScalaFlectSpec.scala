package au.id.jazzy.scalaflect

import org.specs2.mutable.Specification

object ScalaFlectSpec extends Specification {
  import au.id.jazzy.scalaflect.ScalaFlect.toTraverser
  "ScalaFlect" should {
    "be able to look up simple case class properties" in {
      val exampleCaseClass = new ScalaFlect(classOf[ExampleCaseClass])
      val member = exampleCaseClass.reflect(_.property)
      member.toString mustEqual "property"
    }
    "be able to follow a path of case class properties" in {
      val exampleCaseClassParent = new ScalaFlect(classOf[ExampleCaseClassParent])
      val member = exampleCaseClassParent.reflect(_.example.property)
      member.toString mustEqual "example.property"
    }
    "be able to look up simple class properties" in {
      val exampleClass = new ScalaFlect(classOf[ExampleClass])
      val member = exampleClass.reflect(_.property)
      member.toString mustEqual "property"
    }
    "be able to follow a path of class properties" in {
      val exampleClassParent = new ScalaFlect(classOf[ExampleClassParent])
      val member = exampleClassParent.reflect(_.example.property)
      member.toString mustEqual "example.property"
    }
    "be able to traverse collections" in {
      val exampleListCaseClass = new ScalaFlect(classOf[ExampleListCaseClass])
      val member = exampleListCaseClass.reflect(_.list.$.property)
      member.toString mustEqual "list.$.property"
    }

  }
}

case class ExampleCaseClass(property: String)

case class ExampleCaseClassParent(example: ExampleCaseClass)

class ExampleClass(val property: String)

class ExampleClassParent(val example: ExampleClass)

class ExampleListCaseClass(val list: List[ExampleCaseClass])
