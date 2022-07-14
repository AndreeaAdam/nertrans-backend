package ro.nertrans.models;

import lombok.*;
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

    @Field("numberPartner")
    private String numberPartner;

    @Field("CUI")
    private String CUI;

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

    @Field("country")
    private String country;

    @Field("city")
    private String city;

    @Field("VATPayer")
    private boolean VATPayer;

}
