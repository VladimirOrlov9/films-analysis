import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{avg, count}
import plotly.element._
import plotly.layout.{Annotation, Axis, BarMode, Layout}
import plotly.{Bar, Plotly}

import java.text.DecimalFormat
import java.util.Properties
import scala.io.Source

object FilmsAnalysis2 {

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
      .selectExpr("*", "cast(left(release_date, 3) as int) as year")
      .groupBy("year")
      .agg(count("year").as("count"),
        avg("vote_average").as("avg"))
      .where("count != 0")
      .select("year", "avg", "count")
      .orderBy("year")
    //      .show()

    val decForm = new DecimalFormat("#0.0")

    val xValue = streamingSelectDF.select("year")
      .map(f=>f.getInt(0).toString+"0-"+f.getInt(0).toString+"9").collect.toList
    val yValue = streamingSelectDF.select("count").map(f=>f.getLong(0)).collect.toList
    val yValue2 = streamingSelectDF.select("avg")
      .map(f=>decForm.format(f.getDouble(0)).replace(",", "."))
      .collect.toList

    val yValueNew = yValue.map{f => f.toDouble/500}

    val data1 = Seq(Bar(xValue, yValueNew, name = "Количество фильмов"),
      Bar(xValue, yValue2, "Средняя оценка"))

    val annotations = xValue.zip(yValue2).map {
      case (x, y) =>
        Annotation(
          x = x,
          y = y,
          text = y.toString,
          xanchor = Anchor.Left,
          yanchor = Anchor.Bottom,
          showarrow = false
        )
    }++ xValue.zip(yValueNew).map {
      case (x, y) =>
        Annotation(
          x = x,
          y = y,
          text = (y*500).toLong.toString,
          xanchor = Anchor.Right,
          yanchor = Anchor.Bottom,
          showarrow = false
        )
    }


    val layout = Layout()
      .withTitle("Средняя оценка фильмов и их количество по годам выпуска")
      .withAnnotations(annotations)
      .withBarmode(BarMode.Group)
      .withXaxis(Axis(tickangle = 45).withTitle("Год выпуска"))
      .withYaxis(Axis().withShowticklabels(false))
      .withHeight(600)

    Plotly.plot("./Docker/data/analysis_2.html", data1, layout)

  }
}
