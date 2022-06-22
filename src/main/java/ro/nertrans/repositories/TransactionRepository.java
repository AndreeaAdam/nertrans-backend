package ro.nertrans.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import ro.nertrans.models.Transaction;

public interface TransactionRepository extends MongoRepository<Transaction, String> {
    Transaction getByInvoiceId(Long invoiceId);
}
