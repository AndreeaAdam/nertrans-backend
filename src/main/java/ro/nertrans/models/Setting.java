package ro.nertrans.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "setting")
public class Setting {

    @Id
    private String id;

    @Field("smartBillEmail")
    private String smartBillEmail;

    @Field("smartBillToken")
    private String smartBillToken;
}
