package com.spbstu.collection;

import com.spbstu.collection.collecting.entity.Movie;
import com.spbstu.collection.collecting.entity.Page;
import com.spbstu.collection.kafka.KafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class CollectionApplication {

	private final KafkaProducer producer;
	private static final Logger log = LoggerFactory.getLogger(CollectionApplication.class);

	@Autowired
	CollectionApplication(KafkaProducer producer) {
		this.producer = producer;
	}

	public static void main(String[] args) {
		SpringApplication.run(CollectionApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}

	@Bean
	public CommandLineRunner run(RestTemplate restTemplate) throws Exception {
		return args -> {

			for (int i = 1; i <= 500; i++) {
				Page quote = restTemplate.getForObject(
						"https://api.themoviedb.org/3/discover/movie?api_key" +
								"=f2b37941a9a8f2b48e348b6832250846&sort_by=popularity" +
								".desc&page=" + i, Page.class);
				if (quote != null) {
					for (int j = 0; j < quote.results.size(); j++) {

						Movie movie = restTemplate.getForObject(
								"https://api.themoviedb.org/3/movie/" + quote.results.get(j).id +
										"?api_key=f2b37941a9a8f2b48e348b6832250846", Movie.class);

						log.info("Consumed: \n" + movie.toString());
						this.producer.sendMessage(movie);
					}
				}
			}

			System.exit(0);
		};
	}
}