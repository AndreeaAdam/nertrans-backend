package ro.nertrans.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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
    private String smartBillEmail;

    @Field("smartBillToken")
    private String smartBillToken;

    @Field("smartBillFiscalCode")
    private String smartBillFiscalCode;

    @Field("userOffices")
    private ArrayList<OfficeDTO> userOffices;

    @Field("netopiaSignature")
    private String netopiaSignature;

    @Field("netopiaPrivateKey")
    private FileDTO netopiaPrivateKey;

    @Field("netopiaPublicKey")
    private FileDTO netopiaPublicKey;
}
