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



package unicassa.finenews

import org.apache.spark._
import org.apache.spark.sql.hive.HiveContext

object FineNews {
  val local = true

  def main(args: Array[String]): Unit = {
    val conf: SparkConf = new SparkConf()
      .setAppName("FineNews")
      .setMaster(master)

    val sc = new SparkContext(conf)
    sc.setLogLevel("ERROR")

    new HiveContext(sc)

    Experiments.runAllTests()

    sc.stop()
  }

  private def master = {
    if (local) {
      "local[*]"
    } else {
      "spark://master:7077"
    }
  }

}
