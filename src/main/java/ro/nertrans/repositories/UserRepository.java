package ro.nertrans.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import ro.nertrans.models.User;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    User getByEmailIgnoreCase(String email);
    Optional<User> getByEmail(String email);
}
