package ro.nertrans.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartnerEditDTO {

    private String CUI;
    private String name;
    private String address;
    private String email;
    private String telephone;
    private String country;
    private String county;
    private String city;
    private boolean VATPayer;
    private String CIF;
    private String bank;
    private String iban;
    private String contact;
    private String clientCode;
}