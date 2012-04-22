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
