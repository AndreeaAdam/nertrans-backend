package ro.nertrans.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class PayUResponseObject {
    private String paymentStatus;

    private String payuPaymentReference;

    private String code;

    private String message;

    private String status;
}
