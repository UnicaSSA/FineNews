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


package unicassa.finenews.regression

import org.apache.spark.mllib.optimization.SquaredL2Updater
import org.apache.spark.mllib.regression._
import org.apache.spark.mllib.tree.RandomForest
import org.apache.spark.rdd.RDD
import unicassa.finenews._

object Regression {

  def linearRegressionWithSGD(training: RDD[LabeledPoint]): PredictionFunction = {
    training.cache()

    val linearRegression = new LinearRegressionWithSGD

    linearRegression.optimizer
      .setNumIterations(1000)
      .setStepSize(0.75)
      .setUpdater(new SquaredL2Updater())
      .setRegParam(0.01)

    linearRegression.run(training).predict _
  }

  def ridgeRegressionWithSGD(training: RDD[LabeledPoint]): PredictionFunction = {
    training.cache()

    val ridgeRegressionWithSGD = new RidgeRegressionWithSGD

    ridgeRegressionWithSGD.optimizer
      .setNumIterations(1000)
      .setStepSize(0.7)
      .setRegParam(0.01)

    ridgeRegressionWithSGD.run(training).predict _
  }

  def lassoWithSGD(training: RDD[LabeledPoint]): PredictionFunction = {
    training.cache()

    val lasso = new LassoWithSGD()

    lasso.optimizer
      .setNumIterations(1000)
      .setStepSize(0.7)
      .setRegParam(0.01)

    lasso.run(training).predict _
  }

  def randomForest(training: RDD[LabeledPoint]): PredictionFunction = {
    val categoricalFeaturesInfo = Map[Int, Int]()
    val numTrees = 30
    val featureSubsetStrategy = "auto"
    val impurity = "variance"
    val maxDepth = 30
    val maxBins = 32

    RandomForest.trainRegressor(training, categoricalFeaturesInfo,
      numTrees, featureSubsetStrategy, impurity, maxDepth, maxBins).predict _
  }

  def supportVectorRegression(options: String)(training: RDD[LabeledPoint]): PredictionFunction = {
    val svr = new SupportVectorRegression(options)
    svr.train(training)
    SVRModel.predict
  }

  def predict(alg: LearningAlgorithm)(trainingData: RDD[LabeledPoint])(testData: RDD[LabeledPoint]): RDD[Double] = {
    val predictFunction = alg(trainingData)
    testData.map(p => predictFunction(p.features))
  }
}
