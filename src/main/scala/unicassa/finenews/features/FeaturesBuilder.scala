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

import org.apache.spark.ml.feature.{HashingTF, IDF}
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.sql.DataFrame
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.rdd.RDD

object FeaturesBuilder {

  def tfidf(df: DataFrame): DataFrame = {
    if (!df.columns.contains("filteredFeatures")) {
      tfidf(FeaturesSelector.select(df))
    } else {
      df.cache()
      val hashingTF = new HashingTF()
        .setInputCol("filteredFeatures")
        .setOutputCol("tf")
        .setNumFeatures(10000)

      val tfData = hashingTF.transform(df)
      val idf = new IDF().setInputCol("tf").setOutputCol("tfidf")
      val idfModel = idf.fit(tfData)
      idfModel.transform(tfData).drop("tf")
    }
  }

  def build(data: DataFrame): RDD[LabeledPoint] = {
    if (data.columns.contains("tfidf")) {
      data.rdd.map(row =>
        new LabeledPoint(row.getAs[Double]("sentiment_score"), row.getAs[Vector]("tfidf")))
    } else {
      build(tfidf(data))
    }
  }

}
