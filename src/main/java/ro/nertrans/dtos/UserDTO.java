package ro.nertrans.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String firstName;
    private String lastName;
    private String password;
    private String telephone;
    private String email;
    private String address;
    private String employeeCode;
    private String office;
}
