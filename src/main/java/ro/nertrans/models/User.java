package ro.nertrans.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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
    private String email;

    @Field("active")
    private boolean active;

    @Field("actLikeAdmin")
    private boolean actLikeAdmin;

    @Field("telephone")
    private String telephone;

    @Field("address")
    private String address;

    @Field("employeeCode")
    private String employeeCode;

    @Field("office")
    private ArrayList<String> office;

    @Field("roles")
    private ArrayList<UserRoleEnum> roles;

    @Field("registrationDate")
    private LocalDateTime registrationDate;

    @Field("name")
    private String name;
}
