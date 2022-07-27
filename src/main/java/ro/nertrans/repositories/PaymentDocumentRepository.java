package ro.nertrans.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import ro.nertrans.models.PaymentDocument;

import java.util.Optional;

public interface PaymentDocumentRepository extends MongoRepository<PaymentDocument, String> {

    Optional<PaymentDocument> findByDocSeriesAndDocNumber(String docSeries, Long docNumber);

}
