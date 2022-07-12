package ro.nertrans.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartnerDTO {
    private String id;
    private String CUI;
    private String name;
    private String address;
    private String email;
    private String telephone;
    private LocalDateTime date;
    private String userId;
    private String country;
    private String city;
    private boolean VATPayer;
}
