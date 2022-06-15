package ro.nertrans.filters.filteredDTOS;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String employeeCode;
    private String office;
}
