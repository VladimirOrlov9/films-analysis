package com.spbstu.collection;

import com.spbstu.collection.collecting.entity.Movie;
import com.spbstu.collection.collecting.entity.Page;
import com.spbstu.collection.kafka.KafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class CollectionApplication {

	@Value(value = "${tmdb.apikey}")
	private String apiKey;

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
	public CommandLineRunner run(RestTemplate restTemplate) {
		return args -> {

			for (int year = 1920; year <= 2021; year ++) {
				for (int i = 1; i <= 500; i++) {
					Page quote = restTemplate.getForObject(
							"https://api.themoviedb.org/3/discover/movie?include_adult=false" +
									"&api_key=" + apiKey + "&sort_by=popularity" +
									".desc&page=" + i + "&primary_release_year="+year, Page.class);

					if (quote != null) {
						if (quote.results.isEmpty()) {
							i = 501;
							continue;
						}

						for (int j = 0; j < quote.results.size(); j++) {
							try {
								Movie movie = restTemplate.getForObject(
										"https://api.themoviedb.org/3/movie/" + quote.results.get(j).id +
												"?api_key=" + apiKey, Movie.class);

								if (movie != null) {
									log.info("Consumed: \n" + movie.toString());
									this.producer.sendMessage(movie);
								}
							} catch (Exception ex) {
								log.error(ex.getMessage());
							}
						}
					}
				}
			}
			System.exit(0);
		};
	}
}