package ro.nertrans.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import ro.nertrans.models.Partner;

public interface PartnerRepository extends MongoRepository<Partner, String> {
    Partner getByCUIIgnoreCase(String CUI);

}
