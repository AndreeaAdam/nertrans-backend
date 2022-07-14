package ro.nertrans.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginDTO {

    @Getter(value= AccessLevel.NONE)
    @Setter(value=AccessLevel.NONE)
    private String email;
    private String password;

    public String getEmail() {
        return email.toLowerCase();
    }

    public void setEmail(String email) {
        this.email = email.toLowerCase();
    }
}