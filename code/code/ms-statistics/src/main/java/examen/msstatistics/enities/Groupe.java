package examen.msstatistics.enities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Groupe {
    @Id
    private String id; // MongoDB generates the ObjectId and stores it as a hex string

    @Indexed(unique = true)
    private Long idGroupe;

    @DBRef
    private List<Lecture> lectures;

    @DBRef
    private List<Student> students;

    private String name;
    private Date dateGroupe;
}
