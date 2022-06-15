package ro.nertrans.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import ro.nertrans.models.RegActivationCode;

import java.util.ArrayList;

public interface RegActivationCodeRepository extends MongoRepository<RegActivationCode, String> {
    RegActivationCode getByRegistrationCode(String registrationCode);
    ArrayList<RegActivationCode> getByUserId(String userId);

}
