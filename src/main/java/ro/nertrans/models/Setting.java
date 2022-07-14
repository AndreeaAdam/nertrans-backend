package ro.nertrans.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import ro.nertrans.dtos.FileDTO;
import ro.nertrans.dtos.OfficeDTO;
import ro.nertrans.dtos.OfficeNumberDTO;

import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "setting")
public class Setting {

    @Id
    private String id;

    @Field("smartBillEmail")
    @Getter(value= AccessLevel.NONE)
    @Setter(value=AccessLevel.NONE)
    private String smartBillEmail;

    @Field("smartBillToken")
    private String smartBillToken;

    @Field("smartBillFiscalCode")
    private String smartBillFiscalCode;

    @Field("userOffices")
    private ArrayList<OfficeDTO> userOffices;

    @Field("officeNumber")
    private ArrayList<OfficeNumberDTO> officeNumber;

    @Field("euPlatescKey")
    private String euPlatescKey;

    @Field("euPlatescMerchId")
    private String euPlatescMerchId;

    @Field("netopiaSignature")
    private String netopiaSignature;

    @Field("netopiaPrivateKey")
    private FileDTO netopiaPrivateKey;

    @Field("netopiaPublicKey")
    private FileDTO netopiaPublicKey;

    public String getSmartBillEmail() {
        return smartBillEmail.toLowerCase();
    }

    public void setSmartBillEmail(String smartBillEmail) {
        this.smartBillEmail = smartBillEmail.toLowerCase();
    }
}
