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


package unicassa.finenews.data

import unicassa.finenews.features.{FeaturesBuilder, FeaturesSelector}
import org.apache.spark.sql.functions.udf
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.sql.DataFrame
import unicassa.finenews.LearningAlgorithm

object TestSetClassifier {

  def run(rawTraining: DataFrame)(rawTest: DataFrame)(algorithm: LearningAlgorithm): Unit = {
    rawTraining.cache()
    rawTest.cache()

    FeaturesSelector.setDataFrame(rawTraining)
    val trainingData = FeaturesBuilder.tfidf(rawTraining)
    val testData = FeaturesBuilder.tfidf(rawTest)

    testData.cache()
    trainingData.cache()

    val trainingRDD = FeaturesBuilder.build(trainingData)

    val model = algorithm(trainingRDD)

    def udfPredict = udf {features: Vector => model(features)}
    val result = testData.withColumn("sentiment_score", udfPredict(testData("tfidf")))

    DataHandler.writeJson(result)
  }

  def runOnMicroblogs: (LearningAlgorithm) => Unit = {
    val rawTraining = new MicroblogsTrainingLoader(spans = true).data
    val rawTest = new MicroblogsTestLoader(spans = true).data
    run(rawTraining)(rawTest)
  }

  def runOnHeadlines: (LearningAlgorithm) => Unit = {
    val rawTraining = new HeadlinesTrainingLoader().data
    val rawTest = new HeadlinesTestLoader().data
    run(rawTraining)(rawTest)
  }

}