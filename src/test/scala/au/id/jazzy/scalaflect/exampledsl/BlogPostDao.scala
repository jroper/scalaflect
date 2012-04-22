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
  