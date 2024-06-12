package examen.msstatistics.dao;

import examen.msstatistics.enities.Teacher;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TeacherRepository extends MongoRepository<Teacher,Long> {

    Teacher findTeacherByIdTeacher (Long idTeacher);
}
