import java.util.Properties
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{avg, count}
import plotly.layout.{Annotation, Layout}
import plotly.{Bar, Plotly}
import java.text.DecimalFormat
import plotly.element._
import scala.io.Source

object FilmsAnalysis1 {

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
      .orderBy($"vote_average")
      .groupBy("original_language")
      .agg(count("original_language").as("count"),
        avg("vote_average").as("avg"))
      .orderBy("avg")



    val decForm = new DecimalFormat("#0.0")

    val xValue = streamingSelectDF.select("original_language").map(f=>f.getString(0)).collect.toList
    val yValue = streamingSelectDF.select("avg").map(f=>decForm.format(f.getDouble(0)).replace(",", ".")).collect.toList
    val yValue2 = streamingSelectDF.select("count").map(f=>f.getLong(0)).collect.toList

    val data1 = Seq(Bar(xValue, yValue))
    val data2 = Seq(Bar(xValue, yValue2))

    val annotations = xValue.zip(yValue).map {
      case (x, y) =>
        Annotation(
          x = x,
          y = y,
          text = y.toString,
          xanchor = Anchor.Center,
          yanchor = Anchor.Bottom,
          showarrow = false
        )
    }

    val layout = Layout(
      title = "1",
      annotations = annotations
    )

    Plotly.plot("./Docker/data/analysis_1_1.html", data1, layout)

    val annotations1 = xValue.zip(yValue2).map {
      case (x, y) =>
        Annotation(
          x = x,
          y = y,
          text = y.toString,
          xanchor = Anchor.Center,
          yanchor = Anchor.Bottom,
          showarrow = false
        )
    }

    val layout1 = Layout(
      title = "1",
      annotations = annotations1
    )

    Plotly.plot("./Docker/data/analysis_1_2.html", data2, layout1)
  }
}
