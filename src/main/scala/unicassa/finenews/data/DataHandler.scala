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

import java.io.File
import java.net.URI

import org.apache.spark.SparkContext
import org.apache.spark.sql.functions.udf
import org.apache.spark.sql.types._
import org.apache.spark.sql.{Column, DataFrame, Row, SQLContext}
import unicassa.finenews.FineNews
import unicassa.finenews.features.Framester

abstract class DataHandler(path: String) {
  val preprocessed: String = path.substring(0, path.lastIndexOf('.')) + "_parquet"
  lazy val data: DataFrame = loadData().cache()

  private def loadData(): DataFrame = {
    if (preprocessedExists) {
      val sc = SparkContext.getOrCreate()
      SQLContext.getOrCreate(sc).read.parquet(preprocessed)
    }
    else {
      preprocess()
    }
  }

  private def preprocessedExists = {
    if (FineNews.local) {
      new File(preprocessed).exists()
    } else {
      val conf = SparkContext.getOrCreate().hadoopConfiguration
      val fs = org.apache.hadoop.fs.FileSystem.get(new URI("hdfs://master:9000"), conf)
      fs.exists(new org.apache.hadoop.fs.Path(preprocessed))
    }
  }

  private def preprocess(): DataFrame = {
    val df = readData().cache()
    val computeBnsynsetsAndFrames = udf { s: String => new Framester(s).bnsynsetsAndFrames }
    def udfGet(n: Int) = udf { row: Row => row match {
      case Row(b: Seq[String], f: Seq[String]) =>
        if (n == 0) b
        else f
    }}

    val pairs = df.withColumn("pair", computeBnsynsetsAndFrames(df("text")))
    val bnsynsets = pairs.withColumn("bnsynsets", udfGet(0)(pairs("pair")))
    val bnsynsetsAndFrames = bnsynsets.withColumn("frames", udfGet(1)(bnsynsets("pair")))
    val result = bnsynsetsAndFrames.drop("pair")

    result.write.parquet(preprocessed)
    result
  }

  protected def readData(cols: Array[String]): DataFrame = {
    val map = Map("message.body" -> "text", "sentiment score" -> "sentiment_score",
      "title" -> "text", "sentiment" -> "sentiment_score")

    val rename = { c: String => new Column(c).as(map.getOrElse(c, c)) }

    val sc = SparkContext.getOrCreate()
    val data = SQLContext.getOrCreate(sc).read.json(path)
    val result = data.select(cols.map(rename): _*).na.drop

    result.withColumn("id", result("id").cast(StringType))
  }

  protected def readData(): DataFrame

}

object DataHandler {
  import org.json4s.JsonDSL._
  import org.json4s.native.JsonMethods._

  import scala.reflect.io.File

  val prefix: String = {
    if (FineNews.local) {
      "./data/"
    } else {
      "hdfs://master:9000/mattia.atzeni/"
    }
  }

  val microblogTraining: String = prefix + "Microblogs_Trainingdata_withmessages.json"
  val microblogTest: String = prefix + "Microblogs_Testdata_withmessages.json"
  val microblogTrial: String = prefix + "Microblog_Trialdata-full.json"

  val headlineTraining: String = prefix + "Headline_Trainingdata.json"
  val headlineTest: String = prefix + "Headlines_Testdata.json"
  val headlineTrial: String = prefix + "Headline_Trialdata.json"

  def writeJson(data: DataFrame): Unit = {
    def toJson = {(id: String, score: Double) =>
      ("id" -> id) ~ ("sentiment score" -> "%.3f".format(score))
    }
    val list = data.rdd.map { row =>
      val id = row.getAs[String]("id")
      val sentiment = row.getAs[Double]("sentiment_score")
      toJson(id, sentiment)
    }.collect().toList
    val json = compact(render(list))
    File("data/submission.json").writeAll(json)
  }
}
