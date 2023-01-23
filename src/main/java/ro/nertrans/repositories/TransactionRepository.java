package ro.nertrans.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import ro.nertrans.models.Transaction;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TransactionRepository extends MongoRepository<Transaction, String> {
    Transaction getByInvoiceId(Long invoiceId);
}
