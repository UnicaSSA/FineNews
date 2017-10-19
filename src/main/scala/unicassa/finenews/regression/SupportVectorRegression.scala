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

import java.util

import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import weka.classifiers.functions.SMOreg
import weka.core.{Attribute, DenseInstance, Instances, Utils}

import scala.collection.JavaConverters._


class SupportVectorRegression(val options: String) {

  def train(trainingData: RDD[LabeledPoint]): Unit = {
    val dataset = buildDataset(trainingData)
    val classifier = new SMOreg()
    classifier.setOptions(Utils.splitOptions(options))
    classifier.buildClassifier(dataset)
    SVRModel.classifier = classifier
  }

  private def buildDataset(trainingData: RDD[LabeledPoint]): Instances = {
    val classIndex = trainingData.first().features.size
    val attributesRange = (0 to classIndex).toList
    val attributes = attributesRange.map(n => new Attribute(n.toString)).asJava
    val size = trainingData.count().toInt
    val dataset = new Instances("dataset", new util.ArrayList[Attribute](attributes), size)
    dataset.setClassIndex(classIndex)

    trainingData.collect.foreach { p =>
      val array = p.features.toArray
      val instance = new DenseInstance(array.length + 1)
      instance.setDataset(dataset)
      array.zipWithIndex.foreach {
        case (value, index) => instance.setValue(index, value)
      }
      instance.setClassValue(p.label)
      dataset.add(instance)
    }

    dataset
  }
}

object SupportVectorRegression {
  val microblogsOptions: String =
    """-C 1.0 -N 0 -I "weka.classifiers.functions.supportVector.RegSMOImproved -T 0.001 -V -P 1.0E-12 -L 0.001 -W 1"
      |-K "weka.classifiers.functions.supportVector.RBFKernel -G 0.04 -C 250007"""".stripMargin

  val headlinesOptions: String =
    """-C 1.0 -N 0 -I "weka.classifiers.functions.supportVector.RegSMOImproved -T 0.001 -V -P 1.0E-12 -L 0.00005 -W 1"
      |-K "weka.classifiers.functions.supportVector.RBFKernel -G 0.012 -C 250007"""".stripMargin

}
