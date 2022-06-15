package ro.nertrans.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * @Description: Model class that stores each partner
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "partners")
public class Partner {
    @Id
    private String id;

    @Field("registrationCode")
    private String registrationCode;

    @Field("name")
    private String name;

    @Field("address")
    private String address;

    @Field("email")
    private String email;

    @Field("telephone")
    private String telephone;

    @Field("date")
    private LocalDateTime date;

    @Field("userId")
    private String userId;
}
