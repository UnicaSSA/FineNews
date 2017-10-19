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
import unicassa.finenews.Tweet

class MicroblogsTestLoader(path: String = DataHandler.microblogTest, spans: Boolean = false)
  extends MicroblogsLoader(path, spans) {

  def load(): RDD[Tweet] = {
    data.rdd.map(row =>
      Tweet(
        row.getAs[String]("id"),
        row.getAs[String]("text"),
        row.getAs[Seq[String]]("frames").toList
      )
    )
  }

  override protected def readDataSpans(): DataFrame = {
    val df = readData(spansCols)
    def udfClean = udf {s: String => s.replace(";$;", " ")}
    df.withColumn("text", udfClean(df("spans")))
  }
}