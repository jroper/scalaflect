package au.id.jazzy.scalaflect.exampledsl

import au.id.jazzy.scalaflect.{ScalaFlect, Member}
import collection.mutable.ListBuffer


/**
 * Example DSL which is able to select objects out of a list based on equivalence of properties
 */
class ObjectListDao[T](val clazz: Class[T]) {
  private val scalaFlect = new ScalaFlect(clazz)
  private val list = new ListBuffer[T]

  def $[R](f : T => R): QueryField[R] = new QueryField(scalaFlect.reflect(f))
  
  def query(exps: QueryExpression*) = {
    var results = list.toList
    for (exp <- exps) {
      results = results.filter(exp.matches(_))
    }
    results.toList
  }

  def save(obj: T) {
    list += obj
  }
}

class QueryField[R](val field: Member[R]) {
  def %=(value: R): QueryExpression = new QueryExpression {
    def matches(obj: Any) = !(field.get(obj) filter {v: Any => v == value} isEmpty)
  }
}

trait QueryExpression {
  def matches(obj: Any): Boolean 
}
