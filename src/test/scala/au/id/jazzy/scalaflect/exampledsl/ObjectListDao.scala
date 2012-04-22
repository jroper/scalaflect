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
