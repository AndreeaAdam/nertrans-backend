package ro.nertrans.models;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * @Description: Activation codes for when a user registers
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "activationCodes")
public class RegActivationCode {
    @Id
    private String id;

    @Field("email")
    @Getter(value= AccessLevel.NONE)
    @Setter(value=AccessLevel.NONE)
    private String email;

    @Field("userId")
    private String userId;

    @Field("registrationCode")
    private String registrationCode;

    @Field("creationDate")
    private LocalDateTime creationDate;

    @Field("expiryDate")
    private LocalDateTime expiryDate;

    public String getEmail() {
        return email.toLowerCase();
    }

    public void setEmail(String email) {
        this.email = email.toLowerCase();
    }
}