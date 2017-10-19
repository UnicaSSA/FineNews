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

import edu.stanford.nlp.simple.Sentence
import org.apache.spark.SparkContext
import org.apache.spark.ml.feature.NGram
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._
import unicassa.finenews.FineNews

import scala.collection.JavaConverters._
import scala.io.Source

object BOWBuilder {
  private val stopwordsPath = {
    if (FineNews.local) {
      "data/stopwords.txt"
    } else {
      "hdfs://master:9000/mattia.atzeni/stopwords.txt"
    }
  }

  private val stopWords = SparkContext.getOrCreate().broadcast(
    Source.fromFile(stopwordsPath).getLines().toSet
  ).value

  def removeStopWords(text: String): Seq[String] = {
    lemmatize(text.toLowerCase()).filter(w => !stopWords.contains(w))
  }

  def lemmatize(text: String): Seq[String] = {
    if (text != "")
      new Sentence(text.toLowerCase()).lemmas().asScala
    else Seq[String]()
  }

  def lemmatize(data: DataFrame): DataFrame = {
    val udfLemmas = udf { text: String => removeStopWords(text) }
    data.withColumn("lemmas", udfLemmas(data("text")))
  }

  def nGrams(n: Int)(data: DataFrame): DataFrame = {
    val ngram = new NGram().setInputCol("lemmas").setOutputCol(s"nGrams$n").setN(n)
    ngram.transform(data)
  }

  def buildBOW(data: DataFrame): DataFrame = {
    nGrams(3)(
      nGrams(2)(lemmatize(data))
    )
  }

}

