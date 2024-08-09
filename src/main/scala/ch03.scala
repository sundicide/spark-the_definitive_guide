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
    val resourceDataPath = getClass.getResource("/data").getPath

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
    def structuredStreaming() = {
      val staticDataFrame = spark.read.format("csv")
        .option("header", "true")
        .option("inferSchema", "true")
        .load(s"$resourceDataPath/retail-data/by-day/*.csv")

      staticDataFrame.createOrReplaceTempView("retail_data")
      val staticSchema = staticDataFrame.schema

      println(staticSchema)


      import org.apache.spark.sql.functions.{window, column, desc, col}
      staticDataFrame
        .selectExpr(
          "CustomerId",
          "(UnitPrice * Quantity) as total_cost",
          "InvoiceDate"
        )
        .groupBy(
          col("CustomerId"), window(col("InvoiceDate"), "1 day")
        )
        .sum("total_cost")
        .show(5)

      val streamingDataFrame = spark.readStream
        .schema(staticSchema)
        .option("maxFilesPerTrigger", 1)
        .format("csv")
        .option("header", "true")
        .load(s"$resourceDataPath/retail-data/by-day/*.csv")

      streamingDataFrame.isStreaming

      val purchaseByCustomerPerHour = streamingDataFrame
        .selectExpr(
          "CustomerId",
          "(UnitPrice * Quantity) as total_cost",
          "InvoiceDate"
        )
        .groupBy(
          $"CustomerId", window(col("InvoiceDate"), "1 day")
        )
        .sum("total_cost")

      // FIXME: NPE 발생
      purchaseByCustomerPerHour.writeStream
        .format("memory") // memory = store in-memory table
        .queryName("customer_purchases") // the name of the in-memory table
        .outputMode("complete") // complete = all the counts should be in the table
        .start()

      spark.sql(
          """
            | SELECT *
            | FROM customer_purchases
            | ORDER BY `sum(total_cost)` DESC
            |""".stripMargin)
        .show(5)
    }

    def machineLearningAndAnalytics() = {
      import org.apache.spark.sql.functions.date_format

      val staticDataFrame = spark.read.format("csv")
        .option("header", "true")
        .option("inferSchema", "true")
        .load(s"$resourceDataPath/retail-data/by-day/*.csv")

      val preppedDataFrame = staticDataFrame
        .na.fill(0)
        .withColumn("day_of_week", date_format($"InvoiceDate", "EEEE"))
        .coalesce(5)

      val trainDataFrame = preppedDataFrame
        .where("InvoiceDate < '2011-07-01'")
      val testDataFrame = preppedDataFrame
        .where("InvoiceDate >= '2011-07-01'")

      println(trainDataFrame.count())
      println(testDataFrame.count())

      import org.apache.spark.ml.feature.StringIndexer
      val indexer = new StringIndexer()
        .setInputCol("day_of_week")
        .setOutputCol("day_of_week_index")

      println(indexer)
    }
    machineLearningAndAnalytics()

    // Stop the SparkSession
    spark.stop()
  }

}
