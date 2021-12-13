import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{avg, col, count, explode, length, size}
import plotly.element._
import plotly.layout.{Annotation, Axis, Layout}
import plotly.{Bar, Plotly}

import java.text.DecimalFormat
import java.util.Properties
import scala.io.Source

object FilmsAnalysis4 {

  def main(args: Array[String]): Unit = {

    val url = getClass.getResource("application.properties")
    val properties: Properties = new Properties()
    val source = Source.fromURL(url)
    properties.load(source.bufferedReader())

    val spark: SparkSession = SparkSession.builder
      .master(properties.getProperty("spark.master"))
      .appName(properties.getProperty("spark.appName"))
      .config("spark.mongodb.input.uri", "mongodb://localhost:27017")
      .config("spark.mongodb.input.database", "mydb")
      .config("spark.mongodb.input.collection", "data_test")
      .getOrCreate()

    spark.sparkContext.setLogLevel("ERROR")
    import spark.implicits._

    val streamingInputDF = spark.read.format("mongo")
      .option("startingOffsets", "earliest")
      .load()

    val streamingSelectDF = streamingInputDF
      .where("status == \"Released\"")
      .withColumn("count", size($"spoken_languages"))
      .groupBy("count")
      .agg(avg("budget").as("avg"))
      .orderBy("count")

    val xValue = streamingSelectDF.select("count")
      .map(f=>f.getInt(0)).collect.toList
    val yValue = streamingSelectDF
      .select("avg")
      .map(f=> f.getDouble(0).toLong)
      .collect
      .toList


    val data = Seq(Bar(xValue, yValue))

    val annotations = xValue.zip(yValue).map {
      case (x, y) =>
        Annotation(
          x = x,
          y = y,
          text = y.toString,
          xanchor = Anchor.Middle,
          yanchor = Anchor.Bottom,
          showarrow = false
        )
    }

    val layout = Layout()
      .withTitle("Средний бюджет фильмов, в зависимости от количества языков, на которые он переведен")
      .withAnnotations(annotations)
      .withXaxis(Axis(tickmode = TickMode.Array, showticklabels = true,
        title = "Количество языков, на которые переведен фильм"))
      .withYaxis(Axis(tickmode = TickMode.Array, showticklabels = true,
        title = "Средний бюджет ($)"))
      .withHeight(600)

    Plotly.plot("./Docker/data/analysis_4_1.html", data, layout)

  }
}
