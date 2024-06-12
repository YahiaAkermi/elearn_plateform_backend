package examen.msstatistics.API;

import examen.msstatistics.dao.*;
import examen.msstatistics.enities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import static org.bouncycastle.asn1.x500.style.RFC4519Style.name;

@RestController
@RequestMapping("api/v1/stat")
public class Controller {

    @Autowired
    private GroupeRepository groupeRepository;
    @Autowired
    private TeacherRepository teacherRepository;


    //POST ENDPOINTS

    @PostMapping("/addTeacher/{idTeacher}")
    public ResponseEntity<?> addTeacher(@PathVariable Long idTeacher) {

        Teacher teacher = new Teacher() ;
        teacher.setIdTeacher(idTeacher);
        teacher.setGroupes(new ArrayList<Groupe>());

        return ResponseEntity.ok(teacherRepository.save(teacher));
    }


    @PostMapping("/addGroupe/{idGroupe}")
    public ResponseEntity<?> addGroupe(@PathVariable Long idGroupe, @RequestParam String name, @RequestParam Long idTeacher) {
        Teacher teacher = teacherRepository.findTeacherByIdTeacher(idTeacher);

        if (teacher == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Teacher not found.");
        }

        Groupe groupe = new Groupe();
        groupe.setIdGroupe(idGroupe);
        groupe.setName(name);
        groupe.setDateGroupe(new Date());
        groupe.setStudents(new ArrayList<Student>());
        // Save the Groupe first to ensure it has an ID
        groupe.setLectures(new ArrayList<Lecture>());
        groupe = groupeRepository.save(groupe);

        // Now add this groupe to the teacher's list of groupes
        List<Groupe> groupes = teacher.getGroupes();



        groupes.add(groupe);
        teacher.setGroupes(groupes);


        // Save the teacher with the new groupe
        teacherRepository.save(teacher);
        return ResponseEntity.ok(groupe);
    }


    @Autowired
    private LectureRepository lectureRepository;

    @PostMapping("/addLecture/{idLecture}")
    public ResponseEntity<?> addLecture(@PathVariable Long idLecture, @RequestParam String name, @RequestParam double lecturePrice, @RequestParam Long idGroupe) {
        Groupe groupe = groupeRepository.findGroupeByIdGroupe(idGroupe);
        if (groupe == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Group not found.");
        }

        Lecture lecture = new Lecture();
        lecture.setIdLecture(idLecture);
        lecture.setLecturePrice(lecturePrice);
        lecture.setName(name);
        lecture.setPayments(new ArrayList<Payment>());

        // Save the Lecture first to ensure it has an ID
        lecture = lectureRepository.save(lecture);

        List<Lecture> lectures = groupe.getLectures();
        lectures.add(lecture);
        groupe.setLectures(lectures);
        groupeRepository.save(groupe);

        return ResponseEntity.ok(lecture);
    }


    @Autowired
    private StudentRepository studentRepository;





    @PostMapping("/addStudent/{idStudent}")
    public ResponseEntity<?> addStudent(@PathVariable Long idStudent, @RequestParam Long idGroupe) {
        Groupe groupe = groupeRepository.findGroupeByIdGroupe(idGroupe);
        if (groupe == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Group not found.");
        }

        Student student = new Student();
        student.setIdStudent(idStudent);

        // Save the Student first to ensure it has an ID
        student = studentRepository.save(student);
        List<Student> students = groupe.getStudents();

        students.add(student);
        groupe.setStudents(students);



        // Now add this student to the groupe's students
        groupeRepository.save(groupe);

        return ResponseEntity.ok(student);
    }


    @Autowired
    private PaymentRepository paymentRepository;

    @PostMapping("/addPayment/{idPayment}")
    public ResponseEntity<?> addPayment(@PathVariable Long idPayment, @RequestParam Long idLecture) {
        Lecture lecture = lectureRepository.findLectureByIdLecture(idLecture);
        if (lecture == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Lecture not found.");
        }

        Payment payment = new Payment();
        payment.setIdPayment(idPayment);
        payment.setDatePayment(new Date());
        // Save the Payment first to ensure it has an ID
        payment = paymentRepository.save(payment);

        List<Payment> payments = lecture.getPayments();
        payments.add(payment);
        lecture.setPayments(payments);



        lectureRepository.save(lecture);

        return ResponseEntity.ok(payment);
    }


//DELETE ENDPOINTS


    @DeleteMapping("/deleteTeacher/{idTeacher}")
    public ResponseEntity<?> deleteTeacher(@PathVariable Long idTeacher) {
        Teacher teacher = teacherRepository.findTeacherByIdTeacher(idTeacher);
        if (teacher == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Teacher not found.");
        }

        // Delete all associated groups, and their associated lectures, students, and payments
        List<Groupe> groupes = teacher.getGroupes();
        for (Groupe groupe : groupes) {
            // Delete all associated lectures and their payments
            List<Lecture> lectures = groupe.getLectures();
            for (Lecture lecture : lectures) {
                // Delete all associated payments
                List<Payment> payments = lecture.getPayments();
                for (Payment payment : payments) {
                    paymentRepository.delete(payment);
                }
                lectureRepository.delete(lecture);
            }

            // Delete all associated students
            List<Student> students = groupe.getStudents();
            for (Student student : students) {
                studentRepository.delete(student);
            }

            // Delete the group itself
            groupeRepository.delete(groupe);
        }

        // Finally, delete the teacher
        teacherRepository.delete(teacher);
        return ResponseEntity.ok("Teacher and all associated entities deleted successfully.");
    }




    @DeleteMapping("/deleteGroupe/{idGroupe}")
    public ResponseEntity<?> deleteGroupe(@PathVariable Long idGroupe) {
        Groupe groupe = groupeRepository.findGroupeByIdGroupe(idGroupe);
        if (groupe == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Group not found.");
        }

        // Delete all associated lectures and their payments
        List<Lecture> lectures = groupe.getLectures();
        for (Lecture lecture : lectures) {
            // Delete all associated payments
            List<Payment> payments = lecture.getPayments();
            for (Payment payment : payments) {
                paymentRepository.delete(payment);
            }
            lectureRepository.delete(lecture);
        }

        // Delete all associated students
        List<Student> students = groupe.getStudents();
        for (Student student : students) {
            studentRepository.delete(student);
        }

        // Remove the group from associated teacher's list of groupes
        List<Teacher> teachers = teacherRepository.findAll();
        for (Teacher teacher : teachers) {
            List<Groupe> groupes = teacher.getGroupes();
            if (groupes.remove(groupe)) {
                teacher.setGroupes(groupes);
                teacherRepository.save(teacher);
            }
        }

        // Finally, delete the group
        groupeRepository.delete(groupe);
        return ResponseEntity.ok("Group and all associated entities deleted successfully.");
    }


    @DeleteMapping("/deleteLecture/{idLecture}")
    public ResponseEntity<?> deleteLecture(@PathVariable Long idLecture) {
        Lecture lecture = lectureRepository.findLectureByIdLecture(idLecture);
        if (lecture == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Lecture not found.");
        }

        // Remove the lecture from associated group's list of lectures
        List<Groupe> groupes = groupeRepository.findAll();
        for (Groupe groupe : groupes) {
            List<Lecture> lectures = groupe.getLectures();
            if (lectures.remove(lecture)) {
                groupe.setLectures(lectures);
                groupeRepository.save(groupe);
            }
        }

        lectureRepository.delete(lecture);
        return ResponseEntity.ok("Lecture deleted successfully.");
    }


    @DeleteMapping("/deleteStudentFromGroup/{idStudent}")
    public ResponseEntity<?> deleteStudentFromGroup(@PathVariable Long idStudent, @RequestParam Long idGroupe) {
        Groupe groupe = groupeRepository.findGroupeByIdGroupe(idGroupe);
        if (groupe == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Group not found.");
        }

        Student student = studentRepository.findById(idStudent).orElse(null);
        if (student == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Student not found.");
        }

        // Remove the student from the group's list of students
        List<Student> students = groupe.getStudents();
        boolean removed = students.removeIf(s -> s.getIdStudent().equals(idStudent));
        if (!removed) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Student is not part of the specified group.");
        }
        groupe.setStudents(students);
        groupeRepository.save(groupe);

        // Delete the student record from the repository
        studentRepository.delete(student);

        return ResponseEntity.ok("Student removed from group and deleted successfully.");
    }








    @GetMapping("/nbGroupes/{idTeacher}")
    public int getNbGroupes(@PathVariable Long idTeacher) {

        return teacherRepository.findTeacherByIdTeacher(idTeacher).getGroupes().size();

    }

    @GetMapping("/nbtLectures/{idTeacher}")
    public int getNbtLectures(@PathVariable Long idTeacher) {
        List<Groupe> groupes = teacherRepository.findTeacherByIdTeacher(idTeacher).getGroupes();

        int count = 0;
        for (Groupe group : groupes){

            count += group.getLectures().size() ;
        }
        return count;
    }


    @GetMapping("/nbtStudents/{idTeacher}")
    public int getNbtStudents(@PathVariable Long idTeacher) {
        List<Groupe> groupes = teacherRepository.findTeacherByIdTeacher(idTeacher).getGroupes();

        int count = 0;
        for (Groupe group : groupes){

            count += group.getStudents().size() ;
        }
        return count;
    }

    @GetMapping("/nbGStudents/{idGroupe}")
    public int getGStudents(@PathVariable Long idGroupe) {
        return groupeRepository.findGroupeByIdGroupe(idGroupe).getStudents().size();
    }


    @GetMapping("/totalLecture/{idLecture}")

    public double getTotalLectures(@PathVariable Long idLecture) {
        return lectureRepository.findLectureByIdLecture(idLecture).getPayments().size() * lectureRepository.findLectureByIdLecture(idLecture).getLecturePrice();
    }

    @GetMapping("/totalGroupe/{idGroupe}")

    public double getTotalGroupe(@PathVariable Long idGroupe) {
        double total = 0;
        List<Lecture> lectures = groupeRepository.findGroupeByIdGroupe(idGroupe).getLectures();

        for (Lecture lecture : lectures){

            total+= lecture.getLecturePrice() * lecture.getPayments().size();
        }
        return total;
    }

    @GetMapping("/totalTeacher/{idTeacher}")

    public double getTotalTeacher(@PathVariable Long idTeacher) {
        List<Groupe> groupes = teacherRepository.findTeacherByIdTeacher(idTeacher).getGroupes();

        double total=0;

        for (Groupe group : groupes){

            List<Lecture> lectures = group.getLectures() ;

            for (Lecture lecture : lectures){

                total+= lecture.getLecturePrice() * lecture.getPayments().size();
            }

        }
        return total;
    }


    @GetMapping("/topTeacher/{idTeacher}")

    public List<Groupe> getTeacherTop(@PathVariable Long idTeacher) {

        List<Groupe> groupes = teacherRepository.findTeacherByIdTeacher(idTeacher).getGroupes();
        Map<Groupe, Double> groupIncomeMap = new HashMap<>();

        for (Groupe group : groupes) {
            double totalIncome = 0;
            List<Lecture> lectures = group.getLectures();
            for (Lecture lecture : lectures) {
                totalIncome += lecture.getLecturePrice() * lecture.getPayments().size();
            }
            groupIncomeMap.put(group, totalIncome);
        }
        List<Groupe> topGroups = groupIncomeMap.entrySet().stream()
                .sorted(Map.Entry.<Groupe, Double>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        return topGroups;

    }

    @GetMapping("/sortTeacher")

    public List<Teacher> getTopTeachers() {
        // Retrieve all teachers
        List<Teacher> teachers = teacherRepository.findAll();

        // Create a map to store the income of each teacher
        Map<Teacher, Double> teacherIncomeMap = new HashMap<>();

        // Calculate the income for each teacher
        for (Teacher teacher : teachers) {
            double totalIncome = 0;
            List<Groupe> groupes = teacher.getGroupes();
            for (Groupe group : groupes) {
                List<Lecture> lectures = group.getLectures();
                for (Lecture lecture : lectures) {
                    totalIncome += lecture.getLecturePrice() * lecture.getPayments().size();
                }
            }
            teacherIncomeMap.put(teacher, totalIncome);
        }

        // Sort the teachers by income in descending order and get the top 3
        List<Teacher> topTeachers = teacherIncomeMap.entrySet().stream()
                .sorted(Map.Entry.<Teacher, Double>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        return topTeachers;
    }

    @GetMapping("/nbLectures/{idGroupe}")

    public int getNblectures(@PathVariable Long idGroupe) {
        return groupeRepository.findGroupeByIdGroupe(idGroupe).getLectures().size();
    }


    @GetMapping("/nbStudents/{idGroupe}")

    public int getNbStudents(@PathVariable Long idGroupe) {
        groupeRepository.findGroupeByIdGroupe(idGroupe).getStudents();

        return groupeRepository.findGroupeByIdGroupe(idGroupe).getStudents().size();
    }



}