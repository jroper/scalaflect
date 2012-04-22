package au.id.jazzy.scalaflect.exampledsl

import org.specs2.mutable.Specification
import org.specs2.specification.Scope

object BlogPostDaoSpec extends Specification {

  "Blog post DAO" should {
    "return all blog posts by an author" in new setup {
      val results = dao.findByAuthor("James")
      results.size mustEqual 2
    }
    "return all blog posts with a particular tag that are published" in new setup {
      val results = dao.findPublishedByTag("scalaflect")
      results.size mustEqual 2
    }
    "return all blog posts commented on by a particular author" in new setup {
      val results = dao.findPostsCommentedOnByAuthor("Steve")
      results.size mustEqual 2
    }

  }  

  trait setup extends Scope {
    lazy val dao = {
      val dao = new BlogPostDao()

      dao.save(new BlogPost("James", "Statically typed reflection", true,
        Set("scala", "reflect", "scalaflect"),
        List(new Comment("John", "Nice"), new Comment("Steve", "+1"))))

      dao.save(new BlogPost("James", "Queries without Strings", true,
        Set("dsl", "scalaflect"),
        List(new Comment("Steve", "Makes things easy"))))

      dao.save(new BlogPost("John", "Having fun", false, Set("scalaflect"), List()))

      dao.save(new BlogPost("John", "Another post", true, Set(),
        List(new Comment("James", "Another comment"))))

      dao
    }
  }

}
