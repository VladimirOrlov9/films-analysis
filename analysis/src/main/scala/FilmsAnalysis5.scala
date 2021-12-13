import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{avg, count, explode, size}
import plotly.element._
import plotly.layout.{Annotation, Axis, BarMode, Layout}
import plotly.{Bar, Plotly}

import java.text.DecimalFormat
import java.util.Properties
import scala.io.Source

object FilmsAnalysis5 {

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
      .withColumn("exploded", explode($"production_countries"))
      .select($"original_title", $"vote_average", $"exploded.name".alias("production_countries"))
      .groupBy("production_countries")
      .agg(count($"production_countries").as("count"),
        avg("vote_average").as("avg"))
      .orderBy($"count".desc)
      .limit(10)
      .orderBy($"count")
//      .show()

    val decForm = new DecimalFormat("#0.0")

    val xValue = streamingSelectDF.select("production_countries")
      .map(f=>f.getString(0)).collect.toList
    val yValue1 = streamingSelectDF
      .select("count")
      .map(f=> f.getLong(0))
      .collect
      .toList
    val yValue2 = streamingSelectDF
      .select("avg")
      .map(f=> decForm.format(f.getDouble(0)).replace(",", "."))
      .collect
      .toList

    val yValue1New = yValue1.map{f => f.toDouble/1000}

    val data = Seq(Bar(xValue, yValue2, "Средняя оценка"),
      Bar(xValue, yValue1New, "Количество фильмов"))

    val annotations = xValue.zip(yValue1New).map {
      case (x, y) =>
        Annotation(
          x = x,
          y = y,
          text = (y*1000).toLong.toString,
          xanchor = Anchor.Left,
          yanchor = Anchor.Bottom,
          showarrow = false
        )
    }++ xValue.zip(yValue2).map {
      case (x, y) =>
        Annotation(
          x = x,
          y = y,
          text = y.toString,
          xanchor = Anchor.Right,
          yanchor = Anchor.Bottom,
          showarrow = false
        )
    }

    val layout = Layout()
      .withTitle("Количество выпущенных фильмов и их средняя оценка по странам производства")
      .withAnnotations(annotations)
      .withXaxis(Axis(tickmode = TickMode.Array, showticklabels = true,
        title = "Топ-10 стран производителей"))
      .withYaxis(Axis(tickmode = TickMode.Array, showticklabels = false))
      .withHeight(600)
      .withBarmode(BarMode.Group)

    Plotly.plot("./Docker/data/analysis_5.html", data, layout)

  }
}
