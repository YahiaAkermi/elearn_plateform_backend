package examen.msstatistics.dao;

import examen.msstatistics.enities.Lecture;
import examen.msstatistics.enities.Payment;
import examen.msstatistics.enities.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PaymentRepository extends MongoRepository<Payment,Long> {
    Payment findPaymentByIdPayment (Long idPayment);


}
