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

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._

object FeaturesSelector {
  private var mData: DataFrame = _
  private var featuresMap: Map[String, Int] = Map[String, Int]()

  def select(inputData: DataFrame): DataFrame = {
    val data = buildBOW(inputData)

    def udfConcat = udf { lists: Seq[Seq[String]] => lists.foldLeft(Seq[String]())(_ ++ _) }

    val nGrams3 = featuresMap.getOrElse("trigrams", 500)
    val nGrams2 = featuresMap.getOrElse("bigrams", 500)
    val frames = featuresMap.getOrElse("frames", 1000)
    val lemmas = featuresMap.getOrElse("lemmas", 1000)
    val bnsynsets = featuresMap.getOrElse("bnsynsets", 1000)

    val df =
      filterTopFeatures("nGrams3")(nGrams3)(
      filterTopFeatures("nGrams2")(nGrams2)(
      filterTopFeatures("frames")(frames)(
      filterTopFeatures("bnsynsets")(bnsynsets)(
      filterTopFeatures("lemmas")(lemmas)(data)))))

    val rawFeaturesCol = udfConcat(array(
      df("lemmas"), df("nGrams2"), df("nGrams3"), df("bnsynsets"), df("frames")))

    df.withColumn("filteredFeatures", rawFeaturesCol)
  }

  private def filterTopFeatures(col: String)(n: Int)(df: DataFrame) = {

    def udfFilterTopFeatures(topFeatures: Array[String]) =
      udf { features: Seq[String] => features.filter(topFeatures.contains(_)) }

    val topFeatures = new FeaturesRanker(mData, col).topFeatures(n)
    df.withColumn(col, udfFilterTopFeatures(topFeatures)(df(col)))
  }

  def setDataFrame(df: DataFrame): Unit = {
      mData = buildBOW(df)
  }

  def setFeaturesMap(map: Map[String, Int]): Unit = {
    featuresMap = map
  }

  private def buildBOW(data: DataFrame) = {
    if (!data.columns.contains("lemmas")) {
      BOWBuilder.buildBOW(data)
    } else {
      data
    }
  }

}
