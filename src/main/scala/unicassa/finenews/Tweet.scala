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


package unicassa.finenews

import unicassa.finenews.features.Framester

abstract class AbstractTweet {
  val id: String
  val text: String
  val frames: List[String]

  override def equals(that: Any): Boolean = {
    that match {
      case that: AbstractTweet =>
        this.text == that.text
      case _ => false
    }
  }

  override def hashCode(): Int = text.hashCode
}

case class Tweet(
  id: String,
  text: String,
  frames: List[String]) extends AbstractTweet

case class ClassifiedTweet(
  id: String,
  text: String,
  sentimentScore: Double,
  frames: List[String]) extends AbstractTweet

object Tweet {
  def apply(id: String, text: String): Tweet = {
    val parser = new Framester(text)
    val frames = parser.frames
    new Tweet(id, text, frames)
  }
}

object ClassifiedTweet {
  def apply(tweet: AbstractTweet, score: Double): ClassifiedTweet = {
    new ClassifiedTweet(tweet.id, tweet.text, score, tweet.frames)
  }

  def apply(id: String, text: String, score: Double): ClassifiedTweet = {
    apply(Tweet(id, text), score)
  }
}