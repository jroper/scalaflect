/*
 * Copyright 2012 James Roper
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package au.id.jazzy.scalaflect

import org.objectweb.asm.signature.{SignatureVisitor, SignatureReader}
import org.objectweb.asm._
import java.lang.reflect.{Method, Field}
import java.util.concurrent.ConcurrentHashMap

/**
 * Provides type safe reflection on the supplied class
 *
 * @param clazz The class to do reflection on
 * @author James Roper
 */
class ScalaFlect[T](val clazz: Class[T]) {
  private val cache = new ConcurrentHashMap[Class[_], Member[_]]

  /**
   * Reflect on the supplied class.
   *
   * @param f A function that does nothing but return a value that is obtained by calling methods and referencing fields
   *    on the supplied input parameter.  This function will never be invoked, rather, it's byte code will be inspected
   *    to find out what method/field path it invokes.
   * @return A reference to the path of fields/methods invoked by the function
   */
  def reflect[R](f: T => R): Member[R] = {
    // If multiple things invoke this at the same time, the worst that will happen is that the member for a function
    // will be calculated multiple times
    Option(cache.get(f.getClass).asInstanceOf[Member[R]]) getOrElse {
      val visitor = new FunctionVisitor;
      val classAsStream = f.getClass.getClassLoader.getResourceAsStream(f.getClass.getName.replace('.', '/') + ".class");
      new ClassReader(classAsStream).accept(visitor, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES)
      val member = visitor.methodVisitor.member.get.asInstanceOf[Member[R]]
      cache.putIfAbsent(f.getClass, member)
      member
    }
  }

  private class FunctionVisitor extends ClassVisitor(Opcodes.ASM4) {
    var methodVisitor = new FunctionMethodVisitor

    override def visitMethod(access: Int, name: String, desc: String, signature: String, exceptions: Array[String]): MethodVisitor = {
      // If the method is called apply, and accepts this class as a parameter, then it's the one we want
      if (name == "apply") {
        var found: Boolean = false
        new SignatureReader(desc).accept(new SignatureVisitor(Opcodes.ASM4) {
          override def visitParameterType() = new SignatureVisitor(Opcodes.ASM4) {
            override def visitClassType(name: String) {
              if (name.equals(clazz.getName.replace('.', '/'))) {
                found = true
              }
              this
            }
          }
        })
        if (found) {
          return methodVisitor
        }
      }
      null
    }
  }

  private class FunctionMethodVisitor extends MethodVisitor(Opcodes.ASM4) {
    var currentClass: Class[_] = clazz
    var member: Option[Member[_]] = None

    def getParentMember[P](parentType: Class[P]): Option[Member[P]] = {
      member match {
        case None => None
        case Some(p: PartialTraversedTraversableMember) => Some(new TraversableTypeMember(parentType, p.parent))
        case _ => member.asInstanceOf[Option[Member[P]]]
      }
    }

    override def visitMethodInsn(opcode: Int, owner: String, name: String, desc: String) {
      // First, check if it's trying to traverse a collection
      if (owner == ScalaFlect.getClass.getName.replace('.', '/')) {
        // Ignore invocations on conversions
      } else if (owner == classOf[TraversableTraverser[Any]].getName.replace('.', '/')) {
        if (name == "$") {
          // Traversing a collection
          currentClass = null;
          member = Some(new PartialTraversedTraversableMember(member))
        } else {
          throw new IllegalArgumentException("The only allowed method to invoke on TraversableTraverser is $")
        }
      } else {
        // Find the method
        try {
          val method = currentClass.getDeclaredMethod(name)
          currentClass = method.getReturnType
          member = Some(new MethodMember(method, getParentMember(method.getDeclaringClass)))
        } catch {
          case e: NoSuchMethodException => throw new IllegalArgumentException("Unexpected method invocation: " + name + desc, e)
        }
      }
      this
    }

    override def visitTypeInsn(opcode: Int, desc: String) {
      if (opcode == Opcodes.CHECKCAST) {
        currentClass = clazz.getClassLoader.loadClass(desc.replace('/', '.'))
      }
    }

    override def visitFieldInsn(opcode: Int, owner: String, name: String, desc: String) {
      if (opcode == Opcodes.GETSTATIC) {
        // Ignore, it's probably just looking up a static for conversion to TraversableTraverser
      } else {
        try {
          val field = currentClass.getDeclaredField(name);
          currentClass = field.getType
          member = Some(new FieldMember(field, getParentMember(field.getDeclaringClass)))
        } catch {
          case e: NoSuchMethodException => throw new IllegalArgumentException("Unexpected field access: " + name + desc, e)
        }
      }
      this
    }
  }

  private case class PartialTraversedTraversableMember(override val parent: Option[Member[_]]) extends Member[Any] {
    override def name() = "partial$"
    override def clazz() = null
    override def invoke(obj: Any) = obj.asInstanceOf[Traversable[Any]]
  }

}

/**
 * Provides implicit methods for conversions
 */
object ScalaFlect {
  /**
   * Convert the given traversable to a traverser so that its type can be referenced
   *
   * @param traversable The traversable to convert
   * @return A TraversableTraverser
   */
  implicit def toTraverser[T](traversable: Traversable[T]): TraversableTraverser[T] = null
}

/**
 * Used to traverse references through a traversable, using syntax mylist.$.property
 * @tparam T The type of the traversable
 */
trait TraversableTraverser[T] {
  def $: T
}

/**
 * A member.  This may be a field, method, or traversable type
 */
trait Member[R] {
  /**
   * The parent of this member look up
   */
  def parent(): Option[Member[_]] = None

  /**
   * The name of this member
   */
  def name(): String

  /**
   * The type of this member
   */
  def clazz(): Class[R]

  /**
   * Get all values associated with this member from the given object
   * @param obj The object
   * @return The values
   */
  def get(obj: Any): Traversable[R] = parent() map { _.get(obj) } getOrElse { List(obj) } flatMap { invoke(_) }

  protected def invoke(obj: Any): Traversable[R]

  override def toString = (parent map { _.toString + "." } getOrElse { "" }) + name
}

/**
 * A field member
 */
case class FieldMember[R](field: Field, override val parent: Option[Member[_]]) extends Member[R] {
  override def name() = field.getName
  override def clazz() = field.getType.asInstanceOf[Class[R]]
  override def invoke(obj: Any) = List(field.get(obj).asInstanceOf[R])
}

/**
 * A method member
 */
case class MethodMember[R](method: Method, override val parent: Option[Member[_]]) extends Member[R] {
  override def name() = method.getName
  override def clazz() = method.getReturnType.asInstanceOf[Class[R]]
  override def invoke(obj: Any) = List(method.invoke(obj).asInstanceOf[R])
}

/**
 * The type of a traversable
 */
case class TraversableTypeMember[R](override val clazz: Class[R], override val parent: Option[Member[_]]) extends Member[R] {
  override def name() = "$"
  override def invoke(obj: Any) = obj.asInstanceOf[Traversable[R]]
}

