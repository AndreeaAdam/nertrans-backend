package ro.nertrans.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import ro.nertrans.models.PaymentDocument;

public interface PaymentDocumentRepository extends MongoRepository<PaymentDocument, String> {
}
