package ro.nertrans.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MobilPayDTO {
    private double amount;
    private String currency;
    private String orderId;
    private String paymentDocumentId;
    private String details;
}
