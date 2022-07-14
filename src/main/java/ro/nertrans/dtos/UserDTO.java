package ro.nertrans.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String firstName;
    private String lastName;
    private String password;
    private String telephone;
    @Getter(value= AccessLevel.NONE)
    @Setter(value=AccessLevel.NONE)
    private String email;
    private String address;
    private String employeeCode;
    private String office;

    public String getEmail() {
        return email.toLowerCase();
    }

    public void setEmail(String email) {
        this.email = email.toLowerCase();
    }
}
