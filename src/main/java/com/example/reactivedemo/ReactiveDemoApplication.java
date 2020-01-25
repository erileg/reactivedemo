package com.example.reactivedemo;

import javafx.css.StyleableProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import lombok.extern.log4j.Log4j2;
import org.reactivestreams.Publisher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.xml.ws.Provider;
import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@SpringBootApplication
public class ReactiveDemoApplication {
	public static void main(String[] args) {
		SpringApplication.run(ReactiveDemoApplication.class, args);
	}
}

@Component
class IntervalGreetingsProducer {
	Flux<GreetingsResponse> produceGreetings(GreetingsRequest request) {
		return Flux.fromStream(Stream.generate(() -> new GreetingsResponse("Hello " + request.getName() + " @ " + Instant.now())
		)).delayElements(Duration.ofSeconds(1));
	}

	@Bean
	RouterFunction<ServerResponse> route(ReservationRepository reservationRepository) {
		return RouterFunctions.route()
				.GET("/reservations", request -> ok().body(reservationRepository.findAll(), Reservation.class))
				.build();
	}

}

@RestController
@RequiredArgsConstructor
class ReservationRestController {
	private final ReservationRepository reservationRepository;
	private final IntervalGreetingsProducer intervalGreetingsProducer;

	@GetMapping(value = "/sse/{name}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Publisher<GreetingsResponse> greetingsPublisher(@PathVariable String name) {
		return intervalGreetingsProducer.produceGreetings(new GreetingsRequest(name));
	}
}

@Component
@Log4j2
@RequiredArgsConstructor
class SampleDataInitializer {
	private final ReservationRepository reservationRepository;

	@EventListener(ApplicationReadyEvent.class)
	public void initialize() {
		Publisher<Reservation> reservations = Flux.just("Luke", "Han", "Leia", "Ben Obi-Wan Kenobi", "Darth Vader", "C-3PO", "R2-D2", "Chewbacca", "Boba Fett")
				.flatMap(name -> reservationRepository.save(new Reservation(null, name)));

		reservationRepository.deleteAll()
				.thenMany(reservations)
				.thenMany(reservationRepository.findAll())
				.subscribe(log::info);
	}
}

interface ReservationRepository extends ReactiveCrudRepository<Reservation, String> {
	// blank
}