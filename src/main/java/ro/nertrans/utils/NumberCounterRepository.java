package ro.nertrans.utils;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface NumberCounterRepository extends MongoRepository<NumberCounter, String> {
    Optional<NumberCounter> findTopByOrderByIdDesc();
}
