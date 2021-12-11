# films-analysis
## Docker
In this project Apache Kafka, Apache Spark, MongoDB will be launched through the docker, the container configuration file is located in the *Docker* folder.
To initially create and run containers, use the command in a folder with *.yml* file:
```sh
docker-compose up -d --build
```
To turn containers off and on, use the commands:
```sh
docker-compose stop
docker-compose start
```
Removing containers:
```sh
docker-compose down
```
To run Docker on the Windows operating system, you can use the following utility:
> https://docs.docker.com/desktop/windows/install/

## Description of Docker containers
1. Zookeeper. hostname: zookeeper, ports: 2181
2. Kafka. hostname: kafkaserver, ports: 9092, 29092
3. Kafka-UI. GUI for Apache Kafka, available by http://localhost:8090
4. Kafka-Connect. hostname: connect, ports: 8083. By default: kafka-topic=data_test -> db=mydb, collection=data_test.
5. MongoDB. mongo1:27017
6. Mongo1-setup. Just for initial setup of MongoDB
7. Mongo-Express. GUI for MongoDB, available by http://localhost:8091
8. Shell. Container for setup Kafka-Connect and interact with MongoDB by 
> mongosh mongodb://mongo1:27017/?replicaSet=rs0
9. Spark-Master. GUI available by http://localhost:8092. Ports for workers: 7077
10. Spark-Workers. This is spark-worker-a & spark-worker-b (WORKER_CORES=1 WORKER_MEMORY=1G). GUI available by http://localhost:8093 (for a) & http://localhost:8094 (for b)
Shared volumes of Master and Workers:
> ./apps:/opt/spark-apps
> ./data:/opt/spark-data
