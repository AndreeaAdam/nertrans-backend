package ro.nertrans.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import ro.nertrans.models.Setting;

public interface SettingRepository extends MongoRepository<Setting, String> {
}
