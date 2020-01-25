package com.example.reactivedemo;

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
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class ReactiveDemoApplication {
	public static void main(String[] args) {
		SpringApplication.run(ReactiveDemoApplication.class, args);
	}
}

@RestController
@RequiredArgsConstructor
class ReservationRestController{
	private final ReservationRepository reservationRepository;

	@GetMapping("/reservations")
	Publisher<Reservation> reservationPublisher (){
		return reservationRepository.findAll();
	}
}

@Component
@Log4j2
@RequiredArgsConstructor
class SampleDataInitializer {
	private final ReservationRepository reservationRepository;

	@EventListener(ApplicationReadyEvent.class)
	public void initialize() {
		Flux<Reservation> reservations = Flux.just("Heinz", "Foo", "Bar", "Calvin", "Hobbes", "Luke", "Han", "Leia")
				.flatMap(name -> reservationRepository.save(new Reservation(null, name)));

		reservationRepository.deleteAll()
				.thenMany(reservations)
				.thenMany(reservationRepository.findAll())
				.subscribe(log::info);
	}
}

//@Document
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//class Reservation {
//	@Id
//	private String id;
//	private String name;
//}

interface ReservationRepository extends ReactiveCrudRepository<Reservation, String> {

}