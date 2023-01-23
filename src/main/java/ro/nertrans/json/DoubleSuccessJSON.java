package ro.nertrans.json;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoubleSuccessJSON {
    private double number;
    private boolean success;
}
