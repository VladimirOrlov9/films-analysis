import java.util.Properties
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{avg, count, explode, size, sum}
import plotly.layout.{Annotation, Axis, BarMode, Layout}
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
      .config("spark.mongodb.input.uri", properties.getProperty("mongodb.url"))
      .config("spark.mongodb.input.database", properties.getProperty("mongodb.databaseName"))
      .config("spark.mongodb.input.collection", properties.getProperty("mongodb.collectionName"))
      .getOrCreate()

    spark.sparkContext.setLogLevel("ERROR")
    import spark.implicits._

    val inputDF = spark.read.format("mongo")
      .option("startingOffsets", "earliest")
      .load()
      .where("status == \"Released\"")
      .distinct()
//      .where("budget > 0")
//      .where("vote_average > 0")

    var startTime = System.currentTimeMillis()

    var selectDF = inputDF
      .where("status == \"Released\"")
      .where("vote_average > 0")
      .orderBy($"vote_average")
      .groupBy("original_language")
      .agg(count("original_language").as("count"),
        avg("vote_average").as("avg"))
      .where("count > 1000")
      .orderBy("avg")


    val decForm = new DecimalFormat("#0.0")

    var xValue = selectDF.select("original_language").map(f=>f.getString(0)).collect.toList
    var yValue = selectDF.select("avg").map(f=>decForm.format(f.getDouble(0)).replace(",", ".")).collect.toList


    var data1 = Seq(Bar(xValue, yValue))


    var annotations = xValue.zip(yValue).map {
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

    var layout = Layout(
      title = "Распределение оценок по языкам оригинала",
      annotations = annotations
    ).withHeight(600)
      .withXaxis(Axis().withTitle("Язык оригинала"))
      .withYaxis(Axis().withTitle("Средняя оценка"))

    Plotly.plot("./Docker/data/analysis_1_1.html", data1, layout)

    xValue = selectDF.orderBy("count").select("original_language").map(f=>f.getString(0)).collect.toList
    var yValue2 = selectDF.orderBy("count").select("count").map(f=>f.getLong(0)).collect.toList

    var data2 = Seq(Bar(xValue, yValue2))

    var annotations1 = xValue.zip(yValue2).map {
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

    var layout1 = Layout(
      title = "Распределение количества фильмов по языкам оригинала",
      annotations = annotations1
    ).withHeight(600)
      .withXaxis(Axis().withTitle("Язык оригинала"))
      .withYaxis(Axis().withTitle("Количество фильмов"))

    Plotly.plot("/opt/spark-data/analysis_1_2.html", data2, layout1)

    var endTime = System.currentTimeMillis()
    System.out.print("Распределение оценок и количества фильмов по языкам оригинала: " + ((endTime-startTime).toDouble/1000) + " seconds \n")
    startTime = System.currentTimeMillis()

    //--------------- 2 task ------------

    selectDF = inputDF
      .where("status == \"Released\"")
      .selectExpr("*", "cast(left(release_date, 3) as int) as year")
      .groupBy("year")
      .agg(count("year").as("count"),
        avg("vote_average").as("avg"))
      .where("count != 0")
      .select("year", "avg", "count")
      .orderBy("year")


    xValue = selectDF.select("year")
      .map(f=>f.getInt(0).toString+"0-"+f.getInt(0).toString+"9").collect.toList
    var yValueString = selectDF.select("count").map(f=>f.getLong(0)).collect.toList
//    var yValue2String = selectDF.select("avg")
//      .map(f=>decForm.format(f.getDouble(0)).replace(",", "."))
//      .collect.toList

//    var yValueNew = yValueString.map{f => f.toDouble/250}

    data1 = Seq(Bar(xValue, yValueString))

    annotations = xValue.zip(yValueString).map {
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

    layout = Layout()
      .withTitle("Средняя количество фильмов по годам выпуска")
      .withAnnotations(annotations)
      .withBarmode(BarMode.Group)
      .withXaxis(Axis(tickangle = 45).withTitle("Год выпуска"))
      .withYaxis(Axis().withShowticklabels(false).withTitle("Количество фильмов"))
      .withHeight(600)

    Plotly.plot("/opt/spark-data/analysis_2.html", data1, layout)

    endTime = System.currentTimeMillis()
    System.out.print("Средняя количество фильмов по годам выпуска: " + ((endTime-startTime).toDouble/1000) + " seconds\n")
    startTime = System.currentTimeMillis()

    //---------------- 3 task ---------------

    selectDF = inputDF
      .where("status == \"Released\"")
      .where("popularity > 0")
      .withColumn("exploded", explode($"genres"))
      .select($"original_title", $"popularity", $"exploded.name".alias("genres"))
      .groupBy("genres")
      .agg(count("genres").as("count"),
        avg("popularity").as("avg"))
      .orderBy("count")

    //      .show()

    xValue = selectDF.select("genres")
      .map(f=>f.getString(0)).collect.toList
    var yValueLong = selectDF
      .select("count").map(f=>f.getLong(0)).collect.toList


    var data = Seq(Bar(xValue, yValueLong))

    annotations = xValue.zip(yValueLong).map {
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

    layout = Layout()
      .withTitle("Разброс фильмов по жанрам")
      .withAnnotations(annotations)
      .withXaxis(Axis(tickangle = 90, title = "Жанр"))
      .withYaxis(Axis(title = "Количество фильмов"))
      .withHeight(600)

    Plotly.plot("/opt/spark-data/analysis_3_1.html", data, layout)

    var orderedDF = selectDF.orderBy($"avg")
    var xValue1 = orderedDF.select("genres")
      .map(f=>f.getString(0)).collect.toList
    var yValue1 = orderedDF
      .select("avg").map(f=>decForm.format(f.getDouble(0)).replace(",", ".")).collect.toList


    data1 = Seq(Bar(xValue1, yValue1))

    annotations1 = xValue1.zip(yValue1).map {
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

    layout1 = Layout()
      .withTitle("Популярность фильмов по жанрам")
      .withAnnotations(annotations1)
      .withXaxis(Axis(tickangle = 90, title = "Жанр"))
      .withYaxis(Axis(title = "Средняя популярность"))
      .withHeight(600)

    Plotly.plot("/opt/spark-data/analysis_3_2.html", data1, layout1)

    endTime = System.currentTimeMillis()
    System.out.print("Разброс и популярность фильмов по жанрам: " + ((endTime-startTime).toDouble/1000) + " seconds\n")
    startTime = System.currentTimeMillis()

    //----------------4 task-------------------

    selectDF = inputDF
      .where("status == \"Released\"")
      .where("budget > 0")
      .withColumn("count", size($"spoken_languages"))
      .groupBy("count")
      .agg(avg("budget").as("avg"))
      .where("0 < count")
      .orderBy("count")


    var xValueInt = selectDF.select("count")
      .map(f=>f.getInt(0)).collect.toList
    yValue = selectDF
      .select("avg")
      .map(f=> decForm.format(f.getDouble(0)/1000000).replace(",", "."))
      .collect
      .toList


    data = Seq(Bar(xValueInt, yValue))

    annotations = xValueInt.zip(yValue).map {
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

    layout = Layout()
      .withTitle("Средний бюджет фильмов, в зависимости от количества языков, на которые он переведен")
      .withAnnotations(annotations)
      .withXaxis(Axis(tickmode = TickMode.Array, showticklabels = true,
        title = "Количество языков, на которые переведен фильм"))
      .withYaxis(Axis(tickmode = TickMode.Array, showticklabels = true,
        title = "Средний бюджет (млн $)"))
      .withHeight(600)

    Plotly.plot("/opt/spark-data/analysis_4.html", data, layout)

    endTime = System.currentTimeMillis()
    System.out.print("Средний бюджет фильмов, в зависимости от количества языков, на которые он переведен: "
      + ((endTime-startTime).toDouble/1000) + " seconds\n")
    startTime = System.currentTimeMillis()

    //----------------5 task-------------------

    selectDF = inputDF
      .where("status == \"Released\"")
      .withColumn("exploded", explode($"production_countries"))
      .select($"original_title", $"vote_average",
        $"exploded.name".alias("production_countries"))
      .groupBy("production_countries")
      .agg(count($"production_countries").as("count"),
        avg("vote_average").as("avg"))
      .orderBy($"count".desc)
      .limit(10)
      .orderBy($"count")

    xValue = selectDF.select("production_countries")
      .map(f=>f.getString(0)).collect.toList
    yValueLong = selectDF
      .select("count")
      .map(f=> f.getLong(0))
      .collect
      .toList


    data = Seq(Bar(xValue, yValueLong))

    annotations = xValue.zip(yValueLong).map {
      case (x, y) =>
        Annotation(
          x = x,
          y = y,
          text = y.toString,
          xanchor = Anchor.Left,
          yanchor = Anchor.Bottom,
          showarrow = false
        )
    }

    layout = Layout()
      .withTitle("Количество выпущенных фильмов по странам производства")
      .withAnnotations(annotations)
      .withXaxis(Axis(tickmode = TickMode.Array, showticklabels = true,
        title = "Топ-10 стран производителей"))
      .withYaxis(Axis(tickmode = TickMode.Array, showticklabels = false,
        title = "Количество фильмов"))
      .withHeight(600)
      .withBarmode(BarMode.Group)

    Plotly.plot("/opt/spark-data/analysis_5.html", data, layout)

    endTime = System.currentTimeMillis()
    System.out.print("Количество выпущенных фильмов по странам производства: "
      + ((endTime-startTime).toDouble/1000) + " seconds\n")
    startTime = System.currentTimeMillis()

    //----------------6 task-------------------

    selectDF = inputDF
      .where("status == \"Released\"")
      .where("vote_average > 0")
      .where("popularity > 0")
      .selectExpr("*", "cast(left(vote_average, 1) as int) as vote_int")
      .groupBy("vote_int")
      .agg(avg("popularity").as("avg"))
      .select("vote_int", "avg")
      .orderBy("vote_int")

    xValue = selectDF.select("vote_int")
      .map(f=>f.getInt(0).toString+".*").collect.toList.map{y =>
      if (y.equals("0.*")) {
        "0"
      } else {
        y
      }
    }

    yValue = selectDF.select("avg")
      .map(f=>decForm.format(f.getDouble(0)).replace(",", "."))
      .collect.toList


    data1 = Seq(Bar(xValue, yValue))

    annotations = xValue.zip(yValue).map {
      case (x, y) =>
        Annotation()
          .withX(x)
          .withY(y)
          .withText(y)
          .withXanchor(Anchor.Middle)
          .withYanchor(Anchor.Bottom)
          .withShowarrow(false)
    }

    layout = Layout()
      .withTitle("Средняя популярность фильмов в зависимости от оценки")
      .withAnnotations(annotations)
      .withBarmode(BarMode.Group)
      .withXaxis(Axis().withTitle("Оценка"))
      .withYaxis(Axis().withTitle("Средняя популярность"))
      .withHeight(600)

    Plotly.plot("/opt/spark-data/analysis_6.html", data1, layout)

    endTime = System.currentTimeMillis()
    System.out.print("Средняя популярность фильмов в зависимости от оценки: "
      + ((endTime-startTime).toDouble/1000) + " seconds\n")
  }
}
