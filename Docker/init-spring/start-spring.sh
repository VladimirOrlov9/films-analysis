#!/bin/bash

function start_Collection {
  java -version

  case $COLLECTION_TYPE in
  DEMO)
    echo "----------Start Demo Collection----------"
    java -jar collection-demo.jar &
    pid=$!
    wait $pid;;
  FULL)
    echo "----------Start Full Collection----------"
    java -jar collection-full.jar &
    pid=$!
    wait $pid;;
  *) echo "Error: $COLLECTION_TYPE is not an option"
  echo "(Use ENV variable COLLECTION_TYPE as 'DEMO' for Demo Collection, 'FULL' for Full Collection)" ;;
esac

}

function start_Spark {
  ./bin/spark-submit --class FilmsAnalysis1 --master $SPARK_MASTER --deploy-mode client spark-job.jar
}

echo "Type of running is $TASK_RUNNING"

case $TASK_RUNNING in
COLLECTION)
  echo "----------Step 1/1: Start Collection Application----------"
  start_Collection;;
SPARK)
  echo "----------Step 1/1: Start Spark Application----------"
  echo "Waiting for start..."
  sleep 40
  echo "Start..."
  start_Spark;;
ALL)
  sleep 30
  echo "----------Step 1/2: Start Collection Application----------"
  start_Collection
  sleep 30
  echo "----------Step 2/2: Start Spark Application----------"
  start_Spark;;
*) echo "Error: $TASK_RUNNING is not an option"
  echo "(Use ENV variable TASK_RUNNING as 'COLLECTION' for Collection, 'SPARK' for Spark Application, 'ALL' for All Steps)" ;;
esac

