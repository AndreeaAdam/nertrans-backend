package ro.nertrans.filters.filteredDTOS;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchDTO {
    private String office;
    private String active;
    private String name;
}
