#!/bin/bash

function start_Collection {
  java -version
  java -jar collection.jar
}

function start_Spark {
  echo "TODO: start Spark app..."
}

echo "Type of running is $TASK_RUNNING"

case $TASK_RUNNING in
COLLECTION)
  echo "----------Step 1/1: Start Collection Application----------"
  start_Collection;;
SPARK)
  echo "----------Step 1/1: Start Spark Application----------"
  start_Spark;;
ALL)
  echo "----------Step 1/2: Start Collection Application----------"
  start_Collection
  sleep 15
  echo "----------Step 2/2: Start Spark Application----------"
  start_Spark;;
*) echo "Error: $TASK_RUNNING is not an option"
  echo "(Use 'COLLECTION' for Collection, 'SPARK' for Spark Application, 'ALL' for All Steps)" ;;
esac

