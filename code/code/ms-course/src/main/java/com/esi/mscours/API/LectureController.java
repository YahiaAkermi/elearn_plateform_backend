package com.esi.mscours.API;


import com.esi.mscours.DTO.LectureDTO;
import com.esi.mscours.DTO.LectureDtoStudent;
import com.esi.mscours.entities.Document;

import com.esi.mscours.entities.Groupe;
import com.esi.mscours.entities.Lecture;
import com.esi.mscours.entities.StudentJoinGroupe;
import com.esi.mscours.model.Debited;
import com.esi.mscours.model.User;
import com.esi.mscours.proxy.PaymentProxy;
import com.esi.mscours.proxy.StatisticsProxy;
import com.esi.mscours.proxy.UserProxy;
import com.esi.mscours.repository.DocumentRepository;
import com.esi.mscours.repository.GroupeRepository;
import com.esi.mscours.repository.LectureRepository;

import com.esi.mscours.repository.StudentJoinGroupeRepository;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@RestController
@RequestMapping("api/v1")
public class LectureController {



    @Autowired
    private LectureRepository lectureRepository;
    @Autowired
    private GroupeRepository groupeRepository;
    @Autowired
    private  PaymentProxy paymentProxy;
    @Autowired
    private DocumentRepository documentRepository;
    @Autowired
    private StudentJoinGroupeRepository studentJoinGroupeRepository;
    @Autowired
    private UserProxy userProxy;

    @Autowired
    private StatisticsProxy statisticsProxy;

    @GetMapping("/lectures")
    public List<Lecture> getAllLectures() {
        return lectureRepository.findAll();
    }

    @PreAuthorize("hasRole('ROLE_TEACHER')")
    @PostMapping("/add-Lecture")
    public Lecture addLecture(@RequestBody LectureDTO lectureDTO) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH); // Updated date format
        Groupe groupe = groupeRepository.findById(lectureDTO.getIdGroupe()).orElse(null); // Use orElse(null) to avoid NoSuchElementException

        if (groupe != null && groupe.getIdTeacher().equals(lectureDTO.getIdTeacher())) {
            Lecture lecture = new Lecture();
            lecture.setGroupe(groupe);
            lecture.setDate(formatter.parse(lectureDTO.getDate())); // Parsing the combined date and time string
            lecture.setTitle(lectureDTO.getTitle());
            List<Document> documents = new ArrayList<>();
            System.out.println(lectureDTO.getDocs());
            List<Long> docs = lectureDTO.getDocs();
            docs.forEach(doc -> {
                Document document = documentRepository.getById(Long.valueOf(doc));
                documents.add(document);
            });
            lecture.setRoomId(0);
            lecture.setDocumentList(documents);
            Lecture lec=lectureRepository.save(lecture);
            statisticsProxy.addLecture(lec.getIdLecture(),lec.getTitle(),groupe.getLecturePrice(),lectureDTO.getIdGroupe());
            return lec;

        }
        return null;
    }


    @PreAuthorize("hasRole('ROLE_TEACHER')")
    @PatchMapping("/update-lecture/{id}")
    public ResponseEntity<Lecture> updateLecture(@PathVariable Long id, @RequestBody Map<String, Object> payload) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
        Optional<Lecture> optionalLecture = lectureRepository.findById(id);

        if (optionalLecture.isPresent()) {
            Lecture lecture = optionalLecture.get();
            Groupe groupe = lecture.getGroupe();

            if (groupe != null && payload.containsKey("idTeacher") && groupe.getIdTeacher().equals(Long.parseLong(payload.get("idTeacher").toString()))) {
                if (payload.containsKey("title")) {
                    lecture.setTitle(payload.get("title").toString());
                }
                if (payload.containsKey("date")) {
                    String dateStr = payload.get("date").toString().replace("T", " ");
                    lecture.setDate(formatter.parse(dateStr));
                }
                // Update documents if needed
                if (payload.containsKey("docs")) {
                    List<Document> documents = new ArrayList<>();
                    List<?> docs = (List<?>) payload.get("docs");
                    if (docs != null) {
                        docs.forEach(doc -> {
                            if (doc instanceof Integer) {
                                Document document = documentRepository.findById(Long.valueOf((Integer) doc)).orElse(null);
                                if (document != null) {
                                    documents.add(document);
                                }
                            } else if (doc instanceof String) {
                                Document document = documentRepository.findById(Long.valueOf((String) doc)).orElse(null);
                                if (document != null) {
                                    documents.add(document);
                                }
                            }
                        });
                    }
                    lecture.setDocumentList(documents);
                }

                Lecture updatedLecture = lectureRepository.save(lecture);
                return ResponseEntity.ok(updatedLecture);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/student-get-lecture/{id}")

    public Lecture getLecture(@PathVariable(value="id") Long idLecture
    ,@RequestParam(value = "idStudent") Long idStudent) {
        System.out.println(idLecture);
        Lecture lecture = lectureRepository.findById(idLecture).get();
        System.out.println(lecture);

        if(lecture != null) {
            Debited debited = paymentProxy.getPayment(idLecture, idStudent);
            if(debited!=null){
               return lecture;
           }
           else {
               return null;
           }
        }
        else return null;

    }

    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @GetMapping("/student-get-lectures")
    public ResponseEntity<Page<LectureDtoStudent>> getStudentLectures(
            @RequestParam(value = "idStudent") Long idStudent,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(value = "filter", defaultValue = "all") String filter,
            @RequestHeader("Authorization") String authorizationHeader) {

        List<StudentJoinGroupe> studentJoinGroupes = studentJoinGroupeRepository.findStudentJoinGroupesByIdStudent(idStudent);
        List<User> teachers = new ArrayList<>();

        LocalDate today = LocalDate.now();
        LocalDate startDate = LocalDate.MIN;
        LocalDate endDate = LocalDate.MAX;
        boolean applyDateFilter = true;

        switch (filter) {
            case "yesterday":
                startDate = today.minusDays(1);
                endDate = today.minusDays(1);
                break;
            case "today":
                startDate = today;
                endDate = today;
                break;
            case "tomorrow":
                startDate = today.plusDays(1);
                endDate = today.plusDays(1);
                break;
            case "next-week":
                startDate = today.plusDays(1);
                endDate = today.plusDays(7);
                break;
            default:
                applyDateFilter = false;
                break;
        }

        final LocalDate finalStartDate = startDate;
        final LocalDate finalEndDate = endDate;

        boolean finalApplyDateFilter = applyDateFilter;

        List<LectureDtoStudent> lectureDtoStudents = studentJoinGroupes.stream()
                .flatMap(studentJoinGroupe -> {
                    Optional<Groupe> optionalGroupe = groupeRepository.findById(studentJoinGroupe.getIdGroupe());
                    if (optionalGroupe.isPresent()) {
                        Groupe groupe = optionalGroupe.get();
                        List<Lecture> lectures = Optional.ofNullable(groupe.getLectures()).orElse(Collections.emptyList());
                        Long idTeacher = groupe.getIdTeacher();

                        User teacher = teachers.stream()
                                .filter(t -> t.getId().equals(idTeacher))
                                .findFirst()
                                .orElseGet(() -> {
                                    User fetchedTeacher = userProxy.getTeacher(idTeacher, "tocours", authorizationHeader);
                                    teachers.add(fetchedTeacher);
                                    return fetchedTeacher;
                                });

                        return lectures.stream()
                                .filter(lecture -> {
                                    if (finalApplyDateFilter) {
                                        LocalDate lectureDate = lecture.getDate().toInstant()
                                                .atZone(ZoneId.systemDefault())
                                                .toLocalDate();
                                        return !lectureDate.isBefore(finalStartDate) && !lectureDate.isAfter(finalEndDate);
                                    }
                                    return true;
                                })
                                .map(lecture -> {
                                    LectureDtoStudent lectureDtoStudent = new LectureDtoStudent();
                                    lectureDtoStudent.setDate(lecture.getDate().toString());
                                    lectureDtoStudent.setTitle(lecture.getTitle());
                                    lectureDtoStudent.setDocs(lecture.getDocumentList());
                                    lectureDtoStudent.setGroupeName(groupe.getName()); // Removed to prevent serialization issues
                                    lectureDtoStudent.setTeacher(teacher);
                                    try {
                                        Debited debited = paymentProxy.getPayment(lecture.getIdLecture(), idStudent);
                                        lectureDtoStudent.setPaymentStatus(debited != null);
                                    } catch (FeignException.NotFound e) {
                                        lectureDtoStudent.setPaymentStatus(false);
                                    }
                                    lectureDtoStudent.setRoomId(lecture.getRoomId());
                                    lectureDtoStudent.setIdLecture(lecture.getIdLecture());
                                    return lectureDtoStudent;
                                });
                    }
                    return Stream.empty();
                })
                .sorted(Comparator.comparing(LectureDtoStudent::getDate)) // Sorting by date in ascending order
                .collect(Collectors.toList());

        int totalElements = lectureDtoStudents.size();
        int start = Math.min(page * size, totalElements);
        int end = Math.min(start + size, totalElements);
        List<LectureDtoStudent> paginatedList = lectureDtoStudents.subList(start, end);

        Page<LectureDtoStudent> lecturePage = new PageImpl<>(paginatedList, PageRequest.of(page, size), totalElements);

        return ResponseEntity.ok(lecturePage);
    }






    @GetMapping("/lecture/{id}")

    public Lecture getAllGroupe(@PathVariable(value="id") Long idLecture) {
        return lectureRepository.findById(idLecture).get();
    }


    @PreAuthorize("hasRole('ROLE_TEACHER')")
    @GetMapping("/teacher-groupe-lectures/{id}" )
    public  List<Lecture> getTeacherGroupeLectures(@PathVariable(value="id") Long idGroupe, @RequestParam(value = "idTeacher") Long idTeacher) {
        if(groupeRepository.findById(idGroupe).get()!=null && groupeRepository.findById(idGroupe).get().getIdTeacher()==idTeacher){
            List<Lecture> lectures=lectureRepository.findLectureByGroupe(groupeRepository.findById(idGroupe).get());
            return lectures;
        }else return null;
    }

    @PreAuthorize("hasRole('ROLE_TEACHER')")
    @PatchMapping ("/insert-roomId/{idLecture}")
    public ResponseEntity<?> insertIdRoom(@PathVariable(value = "idLecture") Long idLecture, @RequestParam(value = "roomId") int roomId) {

        Lecture lecture = lectureRepository.findById(idLecture).get();
        if (lecture != null) {
            lecture.setRoomId(roomId);
            return ResponseEntity.ok(lectureRepository.save(lecture));
        } else return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

    }

    @PreAuthorize("hasRole('ROLE_TEACHER')")
    @GetMapping("/teacher-lectures")
    public ResponseEntity<Page<Lecture>> getAllTeacherLectures(
            @RequestParam(value = "idTeacher") Long idTeacher,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "filter", defaultValue = "all") String filter) {
        Page<Lecture> teacherLecturesPage;
        List<Groupe> teacherGroups = groupeRepository.findGroupesByIdTeacher(idTeacher);

        List<Lecture> allTeacherLectures = new ArrayList<>();
        for (Groupe groupe : teacherGroups) {
            allTeacherLectures.addAll(groupe.getLectures());
        }

        switch (filter) {
            case "today":
                allTeacherLectures = filterTodayLectures(allTeacherLectures);
                break;
            case "tomorrow":
                allTeacherLectures = filterTomorrowLectures(allTeacherLectures);
                break;
            // Add more cases for other filters as needed
            default:
                // No filtering required
                break;
        }

        int totalElements = allTeacherLectures.size();
        int start = Math.min(page * size, totalElements);
        int end = Math.min(start + size, totalElements);
        List<Lecture> paginatedList = allTeacherLectures.subList(start, end);

        teacherLecturesPage = new PageImpl<>(paginatedList, PageRequest.of(page, size), totalElements);

        return ResponseEntity.ok(teacherLecturesPage);
    }

    private List<Lecture> filterTodayLectures(List<Lecture> lectures) {
        LocalDate today = LocalDate.now();
        return lectures.stream()
                .filter(lecture -> {
                    LocalDate lectureDate = lecture.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    return lectureDate.isEqual(today);
                })
                .collect(Collectors.toList());
    }

    private List<Lecture> filterTomorrowLectures(List<Lecture> lectures) {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        return lectures.stream()
                .filter(lecture -> {
                    LocalDate lectureDate = lecture.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    return lectureDate.isEqual(tomorrow);
                })
                .collect(Collectors.toList());
    }

}
