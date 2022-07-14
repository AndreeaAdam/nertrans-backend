package ro.nertrans.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import ro.nertrans.config.UserRoleEnum;

import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * @Description: Model class that stores each user
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {
    @Id
    private String id;

    @Field("numberUser")
    private String numberUser;

    @Field("firstName")
    private String firstName;

    @Field("lastName")
    private String lastName;

    @Field("password")
    private String password;

    @Field("email")
    @Getter(value=AccessLevel.NONE)
    @Setter(value=AccessLevel.NONE)
    private String email;

    @Field("active")
    private boolean active;

    @Field("telephone")
    private String telephone;

    @Field("address")
    private String address;

    @Field("employeeCode")
    private String employeeCode;

    @Field("office")
    private String office;

    @Field("roles")
    private ArrayList<UserRoleEnum> roles;

    @Field("registrationDate")
    private LocalDateTime registrationDate;

    @Field("name")
    private String name;

    public String getEmail() {
        return email.toLowerCase();
    }

    public void setEmail(String email) {
        this.email = email.toLowerCase();
    }
}
