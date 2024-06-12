package examen.msstatistics;

import examen.msstatistics.dao.*;
import examen.msstatistics.enities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

import java.util.Arrays;
import java.util.Date;

@SpringBootApplication
@EnableFeignClients
@EnableEurekaClient
public class MsStatisticsApplication implements CommandLineRunner {
    @Autowired
    private GroupeRepository groupeRepository;
    @Autowired
    private TeacherRepository teacherRepository;
    @Autowired
    private LectureRepository lectureRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private PaymentRepository paymentRepository;

    public static void main(String[] args) {
        SpringApplication.run(MsStatisticsApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        groupeRepository.deleteAll();
        teacherRepository.deleteAll();
        lectureRepository.deleteAll();
        studentRepository.deleteAll();
        paymentRepository.deleteAll();
        // Create Teacher






    }
}
