package orz.mvnsrch

import java.io.FileWriter

import scala.language.postfixOps
import scala.language.reflectiveCalls

import scala.xml.XML
import scala.util.control.Exception._

import sbt._
import sbt.Keys._

object Main extends Plugin {

  override lazy val settings = Seq(
    commands ++= Seq(mvnsrch)
  )

  val QUOT = "\""

  val AND = s"${QUOT}+${QUOT}"

  val BASE_URL = "http://search.maven.org/solrsearch/select?rows=20&wt=xml"

  type Closer = {
    def close(): Unit
  }

  implicit class AutoCloser[A <: Closer](closer: A) {
    def use[B](f: A => B) = allCatch.andFinally(closer.close).either(f(closer))
  }

  lazy val mvnsrch = Command.args("mvnsrch", "space delimited queries") { (state, args) =>
    val url = s"${BASE_URL}&q=${QUOT}${args.mkString(AND)}${QUOT}"
    println("query: " + url)

    val result = (XML.load(url) \\ "result")(0)

    val numFound = result.attribute("numFound").get.text.toInt

    if (numFound == 0)
      println("not found")
    else {
      println(numFound + " entry found")

      val list = for (doc <- result \ "doc") yield {
        doc \ "str" collect {
          case str @ <str>{text}</str> if (str \ "@name" text) == "g"
            => ('group, text.text)
          case str @ <str>{text}</str> if (str \ "@name" text) == "a"
            => ('artifact, text.text)
          case str @ <str>{text}</str> if (str \ "@name" text) == "latestVersion"
            => ('version, text.text)
        } toMap
      }

      list.zipWithIndex.foreach { case(m, i) =>
        println(f"$i%2d : ${m('group)} %% ${m('artifact)} %% ${m('version)}")
      }

      val status = allCatch either {
        val idx= readLine("enter selection number: ").toInt
        val dsc = list(idx)
        new FileWriter("build.sbt", true).use { w =>
          w.write(s"""\n\nlibraryDependencies += "${dsc('group)}" % "${dsc('artifact)}" % "${dsc('version)}"\n""")
        }
      }

      status.fold(
        (error) => println(error),
        (_)     => println("wrote dependency in build.sbt\nshould be reload")
      )
    }

    state
  }

}
