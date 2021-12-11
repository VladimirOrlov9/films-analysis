# films-analysis
## Apache Kafka in Docker
In this project, Kafka will be launched through the docker, the container configuration file is located in the *DockerKafka* folder.
To initially create and run containers, use the command in a folder with *.yml* file:
```sh
docker-compose up -d
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
> https://docs.docker.com/toolbox/toolbox_install_windows/

If you use this, in order to go to the Kafka manager page, specify not the localhost, but the IP specified in the Docker Quickstart Terminal.
It is also necessary to make an entry in the hosts file with this IP of the following form:
```sh
*IP* kafkaserver
```
