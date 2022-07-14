package ro.nertrans.filters.filteredDTOS;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartnerSearchDTO {

    private String CUI;
    private String name;
    private String address;
    private String email;
    private String userId;

}
