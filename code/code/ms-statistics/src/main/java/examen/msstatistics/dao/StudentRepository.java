package examen.msstatistics.dao;

import examen.msstatistics.enities.Lecture;
import examen.msstatistics.enities.Student;
import examen.msstatistics.enities.Student;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface StudentRepository extends MongoRepository<Student,Long> {
    Student findStudentByIdStudent (Long idStudent);

}
