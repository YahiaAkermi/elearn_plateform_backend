package examen.msstatistics.enities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Lecture {
    @Id
    private String id;

    @Indexed(unique = true)

    private  Long idLecture;

    private double lecturePrice;
    private String name;


    @DBRef
    private List<Payment> payments;

}
