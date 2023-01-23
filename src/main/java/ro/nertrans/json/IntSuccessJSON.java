package ro.nertrans.json;


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
