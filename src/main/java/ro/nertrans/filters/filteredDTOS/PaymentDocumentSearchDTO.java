package ro.nertrans.filters.filteredDTOS;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDocumentSearchDTO {
    private String name;
    private String currency;
    private String paymentMethod;
    private String userId;
    private String partnerName;
    private List<String> statuses;
    private String docSeries;
    private String localReferenceNumber;
    private String startDate;
    private String endDate;
    private LocalDate startExpirationDate;
    private LocalDate endExpirationDate;
    private String operationStatus;
    private Boolean currentExpirationDate;

}
