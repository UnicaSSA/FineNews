/**

Copyright 2017 Mattia Atzeni

This file is part of FineNews.

FineNews is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

FineNews is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with FineNews.  If not, see <http://www.gnu.org/licenses/>.
  */


package unicassa.finenews.features

import scala.sys.process._

class Framester(text: String) {

  lazy val bnsynsetsAndFrames: (List[String], List[String]) = parseJson(framester())
  lazy val bnsynsets: List[String] = bnsynsetsAndFrames._1
  lazy val frames: List[String] = bnsynsetsAndFrames._2

  private def framester(): String = {
    println(text)
    val cmd = Seq("curl", "https://lipn.univ-paris13.fr/framester/en/wfd_json/sentence", "-d",
      "data=\"" + text.replaceAll("&|:", "") + ":b", "-X", "PUT")
    try {
      cmd!!
    } catch {
      case e: Exception =>
        println(e.getMessage)
        framester()
    }
  }

  private def parseJson(json: String): (List[String], List[String]) = {
    val bnsynsetsPattern = "bnsynset: http://babelnet\\.org/rdf/([a-zA-Z0-9]+)".r
    val framesPattern = "frames: \\[(.+)\\]".r

    val start = (List[String](), List[String]())

    json.split("\",\\s*\"").foldRight(start)({
      case (bnsynsetsPattern(value), (b, f)) => (value :: b, f)
      case (framesPattern(values), (b, f)) => (b, parseURLs(values) ++ f)
      case (_, acc) => acc
    })
  }

  private def parseURLs(urls: String): List[String] = {
    val pattern = "u'http://www\\.ontologydesignpatterns\\.org/ont/framenet/abox/frame/([a-zA-Z0-9_]+)'".r

    urls.split(",\\s*").foldRight(List[String]())({
      case (pattern(value), acc) => value :: acc
      case (_, acc) => acc
    })
  }
}
