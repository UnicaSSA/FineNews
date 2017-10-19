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

import org.apache.spark.rdd.RDD

import scala.math._

class Evaluator(gold: RDD[Double], predicted: RDD[Double]) {

  val cosineWeight: Double = predicted.count().toDouble / gold.count()

  def eval: Double = cosineWeight * cosine

  private def cosine = {
    val pairs = gold.zip(predicted)

    val num = pairs.map({
      case (g, p) => g * p
    }).sum

    val den = sqrt(sumSquaredScores(gold)) * sqrt(sumSquaredScores(predicted))

    num / den
  }

  private def sumSquaredScores(rdd: RDD[Double]): Double = {
    rdd.map(pow(_, 2)).sum()
  }
}

object Evaluator {
  def apply(gold: RDD[Double], predicted: RDD[Double]): Evaluator = new Evaluator(gold, predicted)
}
