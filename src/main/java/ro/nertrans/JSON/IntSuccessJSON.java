package ro.nertrans.JSON;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IntSuccessJSON {
    private int number;
    private boolean success;
}
