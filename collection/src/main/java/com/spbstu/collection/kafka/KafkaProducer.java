package com.spbstu.collection.kafka;

import com.spbstu.collection.collecting.entity.Movie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {

    @Value(value = "${kafka.topicName}")
    private String topicName;

    @Autowired
    private KafkaTemplate<String, Movie> kafkaTemplate;

    public void sendMessage(Movie message) {
        this.kafkaTemplate.send(topicName, message);
    }
}
