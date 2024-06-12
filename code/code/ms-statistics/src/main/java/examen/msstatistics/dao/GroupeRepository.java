package examen.msstatistics.dao;

import examen.msstatistics.enities.Groupe;
import examen.msstatistics.enities.Groupe;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface GroupeRepository extends MongoRepository<Groupe,Long> {

    Groupe findGroupeByIdGroupe (Long idGroupe);


}
