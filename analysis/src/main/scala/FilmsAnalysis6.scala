import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{avg, count}
import plotly.element._
import plotly.layout.{Annotation, Axis, BarMode, Layout}
import plotly.{Bar, Plotly}

import java.text.DecimalFormat
import java.util.Properties
import scala.io.Source

object FilmsAnalysis6 {

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
      .selectExpr("*", "cast(left(vote_average, 1) as int) as vote_int")
      .groupBy("vote_int")
      .agg(avg("popularity").as("avg"))
//      .where("count != 0")
      .select("vote_int", "avg")
      .orderBy("vote_int")
//          .show()

    val decForm = new DecimalFormat("#0.0")

    val xValue = streamingSelectDF.select("vote_int")
      .map(f=>f.getInt(0).toString+".*").collect.toList.map{y =>
      if (y.equals("0.*")) {
        "0"
      } else {
        y
      }
    }

    val yValue = streamingSelectDF.select("avg")
      .map(f=>decForm.format(f.getDouble(0)).replace(",", "."))
      .collect.toList


    val data1 = Seq(Bar(xValue, yValue))

    val annotations = xValue.zip(yValue).map {
      case (x, y) =>
        Annotation(
          x = x,
          y = y,
          text = y,
          xanchor = Anchor.Middle,
          yanchor = Anchor.Bottom,
          showarrow = false
        )
    }


    val layout = Layout()
      .withTitle("Средняя популярность фильмов в зависимости от оценки")
      .withAnnotations(annotations)
      .withBarmode(BarMode.Group)
      .withXaxis(Axis().withTitle("Оценка"))
      .withYaxis(Axis().withTitle("Средняя популярность"))
      .withHeight(600)

    Plotly.plot("./Docker/data/analysis_6.html", data1, layout)

  }
}
