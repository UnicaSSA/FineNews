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

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.udf
import unicassa.finenews.ClassifiedTweet

class HeadlinesTrainingLoader(path: String = DataHandler.headlineTraining)
  extends DataHandler(path) {

  override protected def readData(): DataFrame =
    readData(Array("id", "title", "sentiment"))

  def load(): RDD[ClassifiedTweet] = {
    data.rdd.map(row =>
      ClassifiedTweet(
        row.getAs[String]("id"),
        row.getAs[String]("text"),
        row.getAs[Double]("sentiment_score"),
        row.getAs[Seq[String]]("frames").toList
      )
    )
  }

  def writeToCSV(): Unit = {
    val listToString = udf({ l: Seq[String] => l.fold(" ")(_ + " " + _) })

    val temp = data.select("id", "text", "sentiment_score")

    temp.repartition(1).write
      .format("com.databricks.spark.csv")
      .option("header", "true")
      .save(path + "_csv")
  }

}
