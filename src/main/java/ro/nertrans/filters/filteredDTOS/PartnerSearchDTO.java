package ro.nertrans.filters.filteredDTOS;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartnerSearchDTO {

    private String CUI;
    private String name;
    private String address;
    @Getter(value=AccessLevel.NONE)
    @Setter(value=AccessLevel.NONE)
    private String email;
    private String userId;

    public String getEmail() {
        return email.toLowerCase();
    }

    public void setEmail(String email) {
        this.email = email.toLowerCase();
    }
}
