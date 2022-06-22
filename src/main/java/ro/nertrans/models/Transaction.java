package ro.nertrans.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * @Description: Transactions model
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "transactions")
public class Transaction {
    @Id
    private String id;

    /**
     * Cash amount used
     */
    @Field("amount")
    private double amount;

    @Field("currency")
    private String currency;

    @Field("transactionDate")
    private LocalDateTime transactionDate;

    @Field("description")
    private String description;

    @Field("invoiceId")
    private Long invoiceId;

    @Field("paymentDocumentId")
    private String paymentDocumentId;

    @Field("status")
    private String status;

}
