package ro.nertrans.json;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StringSuccessJSON {
    private boolean success;
    private String reason;
}
