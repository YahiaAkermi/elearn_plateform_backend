package examen.msstatistics.dao;

import examen.msstatistics.enities.Lecture;
import examen.msstatistics.enities.Lecture;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LectureRepository extends MongoRepository<Lecture,Long> {
    Lecture findLectureByIdLecture (Long idLecture);

}
