package spark.job1

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import spark.commonmethod.Common
import spark.{Beers, Breweries, Reviews, SessionSpark}


/**
 * Top 20 birrerie con almeno 5 birre diverse
 * con le medie di voti piu alta.(50 recensioni minima per ogni birra
 * (puo cambiare la quantita), vedremo la media di ricensioni per ogni birra).
 */
object TopBreweries extends SessionSpark {

  def executeJob(sparkSession: SparkSession, nBirrerie: Int = 20,beersForBrewery: Int = 5, minRecensioni: Int = 50): Unit = {

    val (beers,reviews,breweries) = readFile(sparkSession)
    val beersRDD = removeFirstRow(beers).map(Beers.extract).keyBy(_.brewery_id)
    val breweriesRDD = removeFirstRow(breweries).map(Breweries.extract).keyBy(_.id)
    val reviewsRDD = removeFirstRow(reviews).map(Reviews.extract).keyBy(_.beer_id)
    val beersAndBreweriesJoin = filterBreweries(beersRDD,breweriesRDD,beersForBrewery)
    val reviewsRDDAveraged = Common.filterAndAvgReviews(reviewsRDD,minRecensioni)

    topNBreweries(beersAndBreweriesJoin,reviewsRDDAveraged).zipWithIndex().filter(_._2 < nBirrerie).saveAsTextFile("giovannim/dataset/output/datasetprogetto/spark/")

  }

  private def removeFirstRow(rdd: RDD[String]): RDD[String] = {
    val firstRow = rdd.first()
    rdd.filter(!_.equals(firstRow))
  }

  private def filterBreweries(rdd: RDD[(Int,Beers)],breweriesRDD: RDD[(Int,Breweries)], beersForBrewery: Int): RDD[(Int,(Beers,Breweries))] ={
    val map = rdd.countByKey()
    rdd.filter(x => map(x._1) >= beersForBrewery).join(breweriesRDD).map(x => (x._2._1.id,x._2))
  }
  private def topNBreweries( beersAndBreweries:RDD[(Int, (Beers, Breweries))], beersAvg:RDD[(Int, Double)]): RDD[(Int, (String, Double))] ={
    beersAvg.join(beersAndBreweries).map(x => (x._2._2._2.id, x._2))
      .mapValues(x => (x,1)).reduceByKey((x,y) => ((x._1._1 + y._1._1,x._1._2),x._2 + y._2)).mapValues(x => (x._1._2._2.name,x._1._1 / x._2))
      .sortBy(_._2._2,ascending = false)
  }
}
