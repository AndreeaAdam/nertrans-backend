package ro.nertrans.JSON;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertySuccessJSON {
    private boolean success;
    private String reason;
    private String properties;
}
