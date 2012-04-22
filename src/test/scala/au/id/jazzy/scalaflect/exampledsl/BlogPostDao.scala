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

/**
 * Blog post DAO
 */
class BlogPostDao extends ObjectListDao(classOf[BlogPost]) {
  import au.id.jazzy.scalaflect.ScalaFlect.toTraverser

  def findByAuthor(author: String) = {
    query(
      $(_.author) %= author
    )
  }
  
  def findPublishedByTag(tag: String) = {
    query(
      $(_.tags.$) %= tag,
      $(_.published) %= true
    )
  }

  def findPostsCommentedOnByAuthor(author: String) = {
    query(
      $(_.comments.$.author) %= author
    )
  }
}

case class BlogPost(author: String, title: String, published: Boolean, tags: Set[String], comments: List[Comment])
case class Comment(author: String, text: String)
  