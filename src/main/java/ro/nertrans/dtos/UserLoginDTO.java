package ro.nertrans.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginDTO {

    private String email;
    private String password;

}