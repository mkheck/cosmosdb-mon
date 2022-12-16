package com.thehecklers.cosmongo;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.logging.Logger;

@SpringBootApplication
public class CosmongoApplication {

	public static void main(String[] args) {
		SpringApplication.run(CosmongoApplication.class, args);
	}

}

@Component
class DataLoader {
	private final UserRepository repo;
	private final Logger logger = Logger.getLogger(this.getClass().getName());

	DataLoader(UserRepository repo) {
		this.repo = repo;
	}

	@PostConstruct
	void loadData() {
		repo.deleteAll()
				.thenMany(Flux.just(new User("Alpha", "Bravo", "123 N 45th St"),
						new User("Charlie", "Delta", "1313 Mockingbird Lane")))
				.flatMap(repo::save)
				.thenMany(repo.findAll())
				.subscribe(user -> logger.info(user.toString()));
	}
}

@RestController
@RequestMapping("/")
class CosmosMongoController {
	private final UserRepository repo;

	CosmosMongoController(UserRepository repo) {
		this.repo = repo;
	}

	@GetMapping
	Flux<User> getAllUsers() {
		return repo.findAll();
	}

	@GetMapping("/oneuser")
	Mono<User> getFirstUser() {
		return repo.findAll().next();
	}

	@PostMapping("/newuser")
	Mono<User> addUser(@RequestBody User user) {
		return repo.save(user);
	}
}

interface UserRepository extends ReactiveCrudRepository<User, String> {
}

@Document
@Data
@NoArgsConstructor
@RequiredArgsConstructor
class User {
	@Id
	String id;
	@NonNull
	String firstName, lastName, address;
}