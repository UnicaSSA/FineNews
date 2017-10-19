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

import org.apache.spark.SparkContext
import org.apache.spark.mllib.regression.{LabeledPoint, RegressionModel}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, SQLContext}
import unicassa.finenews.features.{FeaturesBuilder, FeaturesSelector}

class CrossValidator(val n: Int)(val data: DataFrame)
                    (val predict: RDDPredictionFunction) {

  private val indexed = data.rdd.zipWithIndex()
  private val size = data.count() / n

  def eval: Double = eval(0, List[Double]())

  private def eval(iteration: Int, acc: List[Double]): Double = {
    if (iteration < n) {
      val score = eval(iteration)
      println(score)
      eval(iteration + 1, score :: acc)
    } else {
      finalScore(acc)
    }
  }

  private def eval(iteration: Int) = {
    val (trainingData, testData) = splitData(iteration)
    val (training, test) = featurize(trainingData, testData)
    training.cache()
    test.cache()
    val predictions = predict(training)(test)
    val gold = test.map(_.label)
    Evaluator(gold, predictions).eval
  }

  private def featurize(trainingData: DataFrame, testData: DataFrame): (RDD[LabeledPoint], RDD[LabeledPoint]) = {
    trainingData.cache()
    testData.cache()
    FeaturesSelector.setDataFrame(trainingData)
    val training = FeaturesBuilder.build(trainingData)
    val test = FeaturesBuilder.build(testData)
    (training, test)
  }

  private def splitData(iteration: Int): (DataFrame, DataFrame) = {
    val first = iteration * size
    val last = first + size - 1

    indexed.cache()

    val train = indexed.filter { case (_, index) =>
      index < first || index > last
    }.map(_._1)

    val test = indexed.filter { case (_, index) =>
      first <= index && index <= last
    }.map(_._1)

    val sc = SparkContext.getOrCreate()
    val sqlContext = SQLContext.getOrCreate(sc)

    val trainingData = sqlContext.createDataFrame(train, data.schema)
    val testData = sqlContext.createDataFrame(test, data.schema)

    (trainingData, testData)
  }

  private def finalScore(acc: List[Double]) = {
    if (acc.isEmpty) throw new IllegalArgumentException
    else acc.sum / n
  }

}

object CrossValidator {
  def apply(n: Int)(data: DataFrame)(predict: RDD[LabeledPoint] => RDD[LabeledPoint] => RDD[Double]): CrossValidator =
    new CrossValidator(n)(data)(predict)
}
