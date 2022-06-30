package ro.nertrans.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import ro.nertrans.dtos.FileDTO;

import java.time.LocalDateTime;

/**
 * @Description: Model class that stores each payment document
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "documents")
public class PaymentDocument {
    @Id
    private String id;

    @Field("value")
    private double value;

    @Field("name")
    private String name;

    @Field("currency")
    private String currency;

    @Field("paymentMethod")
    private String paymentMethod;

    @Field("userId")
    private String userId;

    @Field("partnerId")
    private String partnerId;

    @Field("status")
    private String status;

    @Field("fiscalBill")
    private String fiscalBill;

    @Field("attachment")
    private FileDTO attachment;

    @Field("docNumber")
    private Long docNumber;

    @Field("docSeries")
    private String docSeries;

    @Field("localReferenceNumber")
    private String localReferenceNumber;

    @Field("date")
    private LocalDateTime date;

    @Field("partnerName")
    private String partnerName;
}
