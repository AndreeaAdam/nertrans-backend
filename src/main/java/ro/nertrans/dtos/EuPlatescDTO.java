package ro.nertrans.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EuPlatescDTO {
    private double amount;
    private String curr;
    private Long invoice_id;
    private String order_desc;
    private String merchant_id;
    private String timestamp;
    private String nonce;
    private String fp_hash;
    private String paymentDocumentId;
}
