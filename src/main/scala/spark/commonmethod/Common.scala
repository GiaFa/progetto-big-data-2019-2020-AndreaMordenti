package spark.commonmethod

import org.apache.hadoop.fs.Path
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import spark.Reviews

object Common {
  def filterAndAvgReviews(rdd: RDD[(Int,Reviews)], nReviews: Int):RDD[(Int,Double)] = {
    val map = rdd.mapValues(_ => 1L).reduceByKey(_+_).filter(_._2 >= nReviews)
    map.join(rdd).map(x => (x._1,x._2._2))
      .aggregateByKey((0.0,0.0))((avg,count) => (avg._1 + count.overall, avg._2+1),(temp,actual) => (temp._1+actual._1,temp._2+actual._2))
      .mapValues(x => x._1 / x._2)
  }
  def verifyDirectory(sparkSession: SparkSession, resultPath:Path): Unit = {
    val conf = sparkSession.sparkContext.hadoopConfiguration
    val fs = org.apache.hadoop.fs.FileSystem.get(conf)
    if (fs.exists(resultPath)) fs.delete(resultPath, true)
  }
}
