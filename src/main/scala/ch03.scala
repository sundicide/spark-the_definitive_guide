import org.apache.spark.sql.SparkSession

object ch03 {

  case class Flight(DEST_COUNTRY_NAME: String,
                    ORIGIN_COUNTRY_NAME: String,
                    count: BigInt)

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("MongoSparkConnectorIntro")
      .master("local[*]") // Use 'local[*]' for local testing
      .getOrCreate()

    /**
     * Get the path to the resources folder
     * 참고로 getClass 함수는 spark.implicits._를 수행하기 전에 수행해야 한다.
     */
    val resourceDataPath = getClass.getResource("/data/").getPath

    val flightFilePath = (s"$resourceDataPath/flight-data/parquet/2010-summary.parquet/")
    val flightsDF = spark.read.parquet(flightFilePath)

    // Required import for Dataset encoders
    import spark.implicits._

    val flights = flightsDF.as[Flight]

    flights
      .filter(flight_row => flight_row.ORIGIN_COUNTRY_NAME != "Canada")
      .map(flight_row => flight_row)
      .take(5)
      .foreach(println)

    /**
     * Structured Streaming
     */

    val staticDataFrame = spark.read.format("csv")
      .option("header", true)
      .option("inferSchema", "true")
      .load(s"$resourceDataPath/retail-data/by-day/*.csv")

    staticDataFrame.createOrReplaceTempView("retail_data")
    val staticSchema = staticDataFrame.schema

    println(staticSchema)

    // Stop the SparkSession
    spark.stop()
  }

}
