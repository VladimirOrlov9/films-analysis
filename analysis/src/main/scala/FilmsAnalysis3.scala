import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{avg, count, explode}
import plotly.element._
import plotly.layout.{Annotation, Axis, BarMode, Layout}
import plotly.{Bar, Plotly}

import java.text.DecimalFormat
import java.util.Properties
import scala.io.Source

object FilmsAnalysis3 {

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
      .withColumn("exploded", explode($"genres"))
      .select($"original_title", $"popularity", $"exploded.name".alias("genres"))
      .groupBy("genres")
      .agg(count("genres").as("count"),
        avg("popularity").as("avg"))
      .orderBy("count")
//      .show()

    val xValue = streamingSelectDF.select("genres")
      .map(f=>f.getString(0)).collect.toList
    val yValue = streamingSelectDF
      .select("count").map(f=>f.getLong(0)).collect.toList


    val data = Seq(Bar(xValue, yValue, name = "Количество фильмов"))

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
      .withTitle("Разброс фильмов по жанрам")
      .withAnnotations(annotations)
      .withXaxis(Axis(tickangle = 90))
      .withHeight(600)
      .withShowlegend(true)

    Plotly.plot("./Docker/data/analysis_3-1.html", data, layout)

    val decForm = new DecimalFormat("#0.0")

    val orderedDF = streamingSelectDF.orderBy($"avg")
    val xValue1 = orderedDF.select("genres")
      .map(f=>f.getString(0)).collect.toList
    val yValue1 = orderedDF
      .select("avg").map(f=>decForm.format(f.getDouble(0)).replace(",", ".")).collect.toList


    val data1 = Seq(Bar(xValue1, yValue1, name = "Средняя популярность"))

    val annotations1 = xValue1.zip(yValue1).map {
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

    val layout1 = Layout()
      .withTitle("Популярность фильмов по жанрам")
      .withAnnotations(annotations1)
      .withXaxis(Axis(tickangle = 90))
      .withHeight(600)
      .withShowlegend(true)

    Plotly.plot("./Docker/data/analysis_3-2.html", data1, layout1)
  }
}
