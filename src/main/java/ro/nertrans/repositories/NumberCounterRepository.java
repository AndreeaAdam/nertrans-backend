package ro.nertrans.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import ro.nertrans.utils.NumberCounter;

import java.util.Optional;

public interface NumberCounterRepository extends MongoRepository<NumberCounter, String> {
    Optional<NumberCounter> findTopByOrderByIdDesc();
}
