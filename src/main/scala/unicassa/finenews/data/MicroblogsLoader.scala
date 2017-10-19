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

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.udf


abstract class MicroblogsLoader(path: String = DataHandler.microblogTraining, spans: Boolean = false)
  extends DataHandler(path) {

  private val parquet: String = path.substring(0, path.lastIndexOf('.')) + "_parquet"
  override val preprocessed: String =
    if (spans) parquet + "_spans"
    else parquet

  protected def spansCols = Array("id", "spans", "cashtag")
  protected def textCols = Array("id", "text", "cashtag")

  protected def readDataSpans(): DataFrame = {
    val df = readData(spansCols)
    def udfFlat = udf {l: Seq[String] => l.reduce(_ + " " + _)}
    df.withColumn("text", udfFlat(df("spans")))
  }

  protected def readDataText(): DataFrame = {
    readData(textCols)
  }

  override protected def readData(): DataFrame = {
    if (spans) readDataSpans()
    else readDataText()
  }

}
