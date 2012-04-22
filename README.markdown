Statically typed reflection for Scala
=====================================

A problem with many query DSLs in Scala is that they rely on referring to properties of objects using Strings.  This means there are no compile time checks that the query is correct, and small typos can lead to very frustrating and hard to find bugs.

ScalaFlect solves this by allowing statically typed reflection on classes.  It is designed primarily be used as a building block for building DSLs for querying data stores/representations that get mapped to Scala classes, for example, a database object mapper.

An example of what a DSL built on ScalaFlect might look like can be seen here:

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

For an example of this code in action, see https://github.com/jroper/scalaflect/tree/master/src/test/scala/au/id/jazzy/scalaflect/exampledsl

Under the hood
==============

ScalaFlect uses byte code analysis to work out which properties are accessed in the passed in reflection function.  It caches the results so that subsequent uses of that function are very fast.
