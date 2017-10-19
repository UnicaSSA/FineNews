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

import org.apache.spark.mllib.linalg.Vector
import weka.classifiers.functions.SMOreg
import weka.core.{Attribute, Instances, SparseInstance}

import scala.collection.JavaConverters._

object SVRModel {
  var classifier: SMOreg = _

  def predict(features: Vector): Double = {
    val array = features.toArray
    val instance = new SparseInstance(array.length)
    array.zipWithIndex.foreach {
      case (value, index) => instance.setValue(index, value)
    }

    val classIndex = features.size
    val attributesRange = (0 to classIndex).toList
    val attributes = attributesRange.map(n => new Attribute(n.toString)).asJava
    val dataset = new Instances("dataset", new util.ArrayList[Attribute](attributes), 1)
    dataset.setClassIndex(classIndex)

    instance.setDataset(dataset)
    classifier.classifyInstance(instance)
  }
}
