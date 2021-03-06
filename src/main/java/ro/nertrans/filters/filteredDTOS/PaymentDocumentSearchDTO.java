package ro.nertrans.filters.filteredDTOS;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDocumentSearchDTO {
    private String name;
    private String currency;
    private String paymentMethod;
    private String userId;
    private String partnerName;
    private String status;
    private String docSeries;
    private String localReferenceNumber;
    private String startDate;
    private String endDate;

}
