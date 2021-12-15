#!/bin/bash

function test_systems_available {
  COUNTER=0
  until $(curl --output /dev/null --silent --head --fail http://$1:$2); do
      printf '.'
      sleep 2
      let COUNTER+=1
      if [[ $COUNTER -gt 60 ]]; then
        MSG="\nWARNING: Could not reach configured system on http://$1:$2 \nNote: This script requires curl.\n"

          if [[ "$OSTYPE" == "darwin"* ]]; then
            MSG+="\nIf using OSX please try reconfiguring Docker and increasing RAM and CPU. Then restart and try again.\n\n"
          fi

        echo -e $MSG
        exit 1
      fi
  done
}

sleep 5
echo -ne "\n\nWaiting for the systems to be ready.."
test_systems_available $CONNECTOR_HOST $CONNECTOR_PORT

echo -e "\nKafka Connectors adding..."
curl -X POST -H "Content-Type: application/json" --data @sink-connector.json http://$CONNECTOR_HOST:$CONNECTOR_PORT/connectors -w "\n"

sleep 5
echo -e "\nKafka Connectors:"
curl -X GET "http://$CONNECTOR_HOST:$CONNECTOR_PORT/connectors" -w "\n"
