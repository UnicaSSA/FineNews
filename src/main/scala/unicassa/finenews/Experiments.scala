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

import org.apache.spark.sql.DataFrame
import unicassa.finenews.data.{HeadlinesTrainingLoader, MicroblogsTrainingLoader}
import unicassa.finenews.features.{BOWBuilder, FeaturesRanker, FeaturesSelector}
import unicassa.finenews.regression.Regression._
import unicassa.finenews.regression.SupportVectorRegression

object Experiments {

  private val algorithms = List[(RDDPredictionFunction, String)](
    (predict(linearRegressionWithSGD), "LinearRegressionWithSGD"),
    (predict(lassoWithSGD), "LassoWithSGD"),
    (predict(ridgeRegressionWithSGD), "RidgeRegressionWithSGD"),
    (predict(randomForest), "RandomForest")
  )

  def testAlgorithms(algorithms: List[(RDDPredictionFunction, String)])(data: DataFrame): Unit = {
    val crossValidator = CrossValidator(10)(BOWBuilder.buildBOW(data)) _


    algorithms.foreach({ case (alg, name) =>
      println(s"************* $name *************")
      println()
      testFeaturesCombination(crossValidator(alg))
      println()
    })
  }

  def testFeaturesCombination(validator: CrossValidator): Unit = {
    val combinations = 0 to 6
    combinations.foreach { combination =>
      val map = featuresMap(combination)
      FeaturesSelector.setFeaturesMap(map)
      val score = validator.eval
      println(s"Score: $score")
      println()
    }
  }

  private def featuresMap(n: Int): Map[String, Int] = {
    val combination = String.format("%3s", n.toBinaryString).replace(' ', '0')

    val booleanList = List(
      ("lemmas", combination.charAt(0) == '0'),
      ("bigrams", combination.charAt(0) == '0'),
      ("trigrams", combination.charAt(0) == '0'),
      ("bnsynsets", combination.charAt(1) == '0'),
      ("frames", combination.charAt(2) == '0')
    )

    printFeatures(booleanList)

    booleanList.filter {
      case (key: String, value: Boolean) => !value
    }.toMap.mapValues(_ => 0)

  }

  def printFeatures(booleanList: List[(String, Boolean)]): Unit= {
    val buf = new StringBuilder()
    booleanList.foreach({
      case (key, value) =>
        if (value) buf ++= s"$key, "
    })
    println(s"Features: ${buf.dropRight(2)}")
  }

  def runTestsOnMicroblogs(spans: Boolean): Unit = {
    println("************************************************************")
    println(s"\tRunning tests on Microblogs - spans = $spans")
    println("************************************************************")
    println("\n")

    val microblogsAlgorithms = algorithms :+
      ((predict(supportVectorRegression(SupportVectorRegression.microblogsOptions)) _, "SupportVectorRegression"))

    val data = new MicroblogsTrainingLoader(spans = spans).data

    testAlgorithms(microblogsAlgorithms)(data)
  }

  def runTestsOnHeadlines(): Unit = {
    println("************************************************************")
    println(s"\tRunning tests on Headlines")
    println("************************************************************")
    println("\n")

    val headlinesAlgorithms = algorithms :+
      ((predict(supportVectorRegression(SupportVectorRegression.headlinesOptions)) _, "SupportVectorRegression"))

    val data = new HeadlinesTrainingLoader().data

    testAlgorithms(headlinesAlgorithms)(data)
  }

  def runTestsOnMicroblogs(): Unit = {
    runTestsOnMicroblogs(spans = false)
    runTestsOnMicroblogs(spans = true)
  }

  def runAllTests(): Unit = {
    runTestsOnMicroblogs()
    runTestsOnHeadlines()
  }


  def topWords(df: DataFrame): Unit = {
    val data = BOWBuilder.buildBOW(df)
    val topLemmas = new FeaturesRanker(data, "lemmas").featureScores.repartition(1)

    println("Positive:")
    topLemmas.filter(_._2 >= 0).take(10).foreach(println)


    println("Negative:")
    topLemmas.filter(_._2 <= 0).take(10).foreach(println)
  }
}
