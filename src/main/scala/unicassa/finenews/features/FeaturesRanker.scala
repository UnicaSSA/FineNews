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

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.DataFrame

class FeaturesRanker(data: DataFrame, column: String) {
  data.cache()
  private val size = data.count()
  private val averageScore = computeAverageScore()
  private val seq = data.rdd.collect()

  lazy val featureScores: RDD[(String, Double)] = {
    computeWordsCorrelation().sortBy({
      case (word, score) => score.abs
    }, ascending = false).cache()
  }

  private def computeLemmas(): Set[String] = {
    seq.flatMap(_.getAs[Seq[String]](column)).toSet
  }

  private def correlation(word: String) = {
    seq.map { row =>
      val lemmas = row.getAs[Seq[String]](column)
      val score = row.getAs[Double]("sentiment_score")
      weight(word, lemmas) * (score - averageScore)
    }.sum / size
  }

  private def computeAverageScore() = {
    val sum = data.rdd.map(_.getAs[Double]("sentiment_score")).sum
    sum / size
  }

  private def weight(word: String, lemmas: Seq[String]) = {
    if (lemmas.contains(word)) 1
    else -1
  }

  private def computeWordsCorrelation(): RDD[(String, Double)] = {
    val lemmas = computeLemmas()
    val res = lemmas.map(lemma => (lemma, correlation(lemma))).toList
    SparkContext.getOrCreate().parallelize(res)
  }

  def topFeatures(n: Int): Array[String] = {
    featureScores.map(_._1).take(n)
  }
}
