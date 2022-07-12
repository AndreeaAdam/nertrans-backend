package ro.nertrans.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nonapi.io.github.classgraph.json.Id;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NumberCounter {

    @Id
    private String id;

    @Field("seqUser")
    private long seqUser;

    @Field("seqPartner")
    private long seqPartner;

    public NumberCounter(long number) {
    }
}
