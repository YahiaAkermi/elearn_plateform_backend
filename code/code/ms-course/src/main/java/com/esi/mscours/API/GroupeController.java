package com.esi.mscours.API;

import com.esi.mscours.DTO.GroupeDTO;
import com.esi.mscours.DTO.LectureDTO;
import com.esi.mscours.DTO.PayLecturesDto;
import com.esi.mscours.entities.*;

import com.esi.mscours.entities.Module;
import com.esi.mscours.model.User;
import com.esi.mscours.proxy.PaymentProxy;
import com.esi.mscours.proxy.StatisticsProxy;
import com.esi.mscours.proxy.UserProxy;
import com.esi.mscours.repository.GroupeRepository;
import com.esi.mscours.repository.LectureRepository;
import com.esi.mscours.repository.ModuleRepository;
import com.esi.mscours.repository.StudentJoinGroupeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("api/v1")
public class GroupeController {
    @Autowired
    private GroupeRepository groupeRepository;
    @Autowired
    private ModuleRepository moduleRepository ;
    @Autowired
    private LectureRepository lectureRepository ;
    @Autowired
    private StudentJoinGroupeRepository studentJoinGroupeRepository;
    @Autowired
    private UserProxy userProxy;
    @Autowired
    private PaymentProxy paymentProxy;
    @Autowired
    private StatisticsProxy statisticsProxy;


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/groupes")
    public List<Groupe> getAllGroupes() {
        return groupeRepository.findAll();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/groupes/{id}")
    public Groupe getAllGroupe(@PathVariable(value="id") Long idGroupe) {
        return groupeRepository.findById(idGroupe).get();
    }

    // Teacher add Groupe

    @PreAuthorize("hasRole('ROLE_TEACHER')")
    @PostMapping("/add-group")
    public ResponseEntity<?> addGroupe(@RequestBody GroupeDTO groupeDTO) {
        Optional<Module> moduleOpt = moduleRepository.findById(groupeDTO.getIdModule());
        if (moduleOpt.isPresent()) {
            Module module = moduleOpt.get();
            Groupe groupe = new Groupe();
            groupe.setModule(module);
            groupe.setMax(groupeDTO.getMax());
            groupe.setName(groupeDTO.getName());
            groupe.setLecturePrice(groupeDTO.getLecturePrice());
            groupe.setIdTeacher(groupeDTO.getIdUser());
            groupe.setImage(groupeDTO.getImage());
            groupe.setLectureDay(groupeDTO.getLectureDay());
            groupe.setInitialLecturesNumber(groupeDTO.getInitialLecturesNumber());
            groupe.setMinMustPayLecturesNumber(groupeDTO.getMinMustPayLecturesNumber());
            groupe.setStatus(GroupeStatus.PENDING);

            // Saving the group first to generate an ID
            groupe = groupeRepository.save(groupe);

            // Check if lectures have been provided and their count matches the initialLecturesNumber
            if (groupeDTO.getLectures() == null || groupeDTO.getLectures().size() != groupeDTO.getInitialLecturesNumber()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid number of lectures provided.");
            }

            // Creating lectures based on the provided data
            List<Lecture> lectures = new ArrayList<>();
            for (LectureDTO lectureDTO : groupeDTO.getLectures()) {
                Lecture lecture = new Lecture();
                lecture.setTitle(lectureDTO.getTitle());
                // Parse the date and time
                LocalDateTime dateTime = LocalDateTime.parse(lectureDTO.getDate(), DateTimeFormatter.ISO_DATE_TIME);
                // Convert to Date
                Date date = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
                lecture.setDate(date);
                lecture.setGroupe(groupe);
                lecture.setDocumentList(null); // Setting docs to null
                lectures.add(lecture);
            }
            lectureRepository.saveAll(lectures);

            groupe.setLectures(lectures);

            return ResponseEntity.ok(groupeRepository.save(groupe));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Module not found.");
        }
    }



    // Etudiant Join Groupe

    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @PostMapping("/sign-up-to-group")
    public ResponseEntity<?> affectGroupes(@RequestBody Map<String, Object> payload) {
        if (payload.get("idGroupe") != null && payload.get("idStudent") != null && payload.get("name") != null) {
            Long idGroupe = Long.valueOf(payload.get("idGroupe").toString());
            Long idStudent = Long.valueOf(payload.get("idStudent").toString());
            Long idWallet = Long.valueOf(payload.get("idWallet").toString());

            // Extract lecture IDs from payload
            List<Integer> lectureIdsInteger = (List<Integer>) payload.get("lectures");
            List<Long> lectureIds = new ArrayList<>();
            for (Integer id : lectureIdsInteger) {
                lectureIds.add(id.longValue());
            }

            if (groupeRepository.findById(idGroupe).isPresent()) {
                Groupe groupe = groupeRepository.findById(idGroupe).get();
                ArrayList<StudentJoinGroupe> students = new ArrayList<>(groupe.getStudents());

                if (studentJoinGroupeRepository.findStudentJoinGroupeByIdGroupeAndIdStudent(idGroupe, idStudent) != null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Student already joined the group.");
                } else if (groupe.getMax() > students.size()) {
                    // Fetch the lecture IDs associated with the group
                    List<Lecture> lectures = lectureRepository.findLectureByGroupe(groupe);

                    // Prepare the payment details
                    PayLecturesDto payLecturesDto = new PayLecturesDto();
                    payLecturesDto.setWalletId(idWallet);
                    payLecturesDto.setGroupId(idGroupe);
                    payLecturesDto.setLectureIds(lectureIds); // Set the limited number of lecture IDs
                    payLecturesDto.setTeacherId(groupe.getIdTeacher());
                    payLecturesDto.setAmount(groupe.getLecturePrice() * lectureIds.size());
                    payLecturesDto.setTypeId(1);

                    // Call the payment service
                    ResponseEntity<?> paymentResponse = paymentProxy.joinGroupe(payLecturesDto);
                    if (paymentResponse.getStatusCode().is2xxSuccessful()) {
                        students.add(studentJoinGroupeRepository.save(new StudentJoinGroupe(null, idGroupe, idStudent, payload.get("name").toString())));
                        statisticsProxy.addStudent(idStudent,idGroupe);
                        groupe.setStudents(students);
                        Groupe updatedGroupe = groupeRepository.save(groupe);
                        return ResponseEntity.ok(updatedGroupe);
                    } else {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment failed.");
                    }
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Group is full.");
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Group not found.");
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid payload.");
        }
    }


    @PreAuthorize("hasRole('ROLE_TEACHER')")
    @DeleteMapping("/delete-student-from-groupe/{idGroupe}/{idTeacher}/{idStudent}")
    public ResponseEntity<?> deleteGroupe(@PathVariable Long idGroupe,@PathVariable Long idTeacher,@PathVariable Long idStudent){
        Groupe groupe=groupeRepository.findById(idGroupe).get();
        if(groupe!=null){
            if(groupe.getIdTeacher()!=idTeacher){
               return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The teacher is not the owner of this group.");
            }else {
                StudentJoinGroupe studentJoinGroupe=studentJoinGroupeRepository.findStudentJoinGroupeByIdGroupeAndIdStudent(idGroupe,idStudent);
                if(studentJoinGroupe!=null){
                    List<StudentJoinGroupe> students=groupe.getStudents();
                    students.remove(studentJoinGroupe);
                    studentJoinGroupeRepository.deleteById(studentJoinGroupe.getId());
                    statisticsProxy.deleteStudentFromGroup(idStudent,idGroupe);
                    return ResponseEntity.status(HttpStatus.OK).body("Deleted successfully.");
                }else
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Student is not a member of the group.");
            }

        }else  return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Couldn't find the group.");

    }

    // Get Student Groupes
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @GetMapping("/student-groupes" )
    public  List<Groupe> getStudentGroupes( @RequestParam Long idStudent) {

        List<Groupe> groupes = new ArrayList<>();

        List<StudentJoinGroupe> studentJoinGroupes = studentJoinGroupeRepository.findStudentJoinGroupesByIdStudent(idStudent);
        studentJoinGroupes.forEach(e-> {
            groupes.add(groupeRepository.findById(e.getIdGroupe()).get());

        });


        return groupes;

    }


    // Get Teacher Groupes

    @PreAuthorize("hasRole('ROLE_TEACHER')")

    @GetMapping("/teacher-groupes" )
    public  List<Groupe> getTeacherGroupes( @RequestParam Long idTeacher) {

        List<Groupe> groupes = groupeRepository.findGroupesByIdTeacher(idTeacher) ;
        return groupes;

    }

    @PreAuthorize("hasRole('ROLE_TEACHER')")
    @GetMapping("/teacher-groupe/{id}" )
    public  Groupe getTeacherGroupe( @PathVariable(value="id") Long idGroupe,@RequestParam(value = "idTeacher") String idTeacher) {
        if(groupeRepository.findById(idGroupe).isPresent()
                && groupeRepository.findById(idGroupe).get().getIdTeacher().equals(Long.valueOf(idTeacher))){
            return groupeRepository.findById(idGroupe).get();
        }else return null;
    }




    @PreAuthorize("hasRole('ROLE_TEACHER')")
    @PatchMapping("/groupes/{id}")
    public ResponseEntity<Groupe> updateGroupeByPatch(
            @PathVariable(value = "id") Long idGroupe,
            @RequestBody Map<String, Object> payload) {

        Optional<Groupe> optionalGroupe = groupeRepository.findById(idGroupe);
        if (optionalGroupe.isPresent()) {
            Groupe groupe = optionalGroupe.get();
            Long idTeacher = Long.valueOf(payload.get("idTeacher").toString());
            if (groupe.getIdTeacher().equals(idTeacher)) {
                // Only allow updating specified fields
                if (payload.containsKey("name")) {
                    groupe.setName(payload.get("name").toString());
                }
                if (payload.containsKey("image")) {
                    groupe.setImage(payload.get("image").toString());
                }
                if (payload.containsKey("max")) {
                    int maxStudents = Integer.parseInt(payload.get("max").toString());
                    if (maxStudents > 40) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
                    }
                    groupe.setMax(maxStudents);
                }
                if (payload.containsKey("lecturePrice")) {
                    groupe.setLecturePrice(Double.valueOf(payload.get("lecturePrice").toString()));
                }
                if (payload.containsKey("minMustPayLecturesNumber")) {
                    int minMustPayLecturesNumber = Integer.parseInt(payload.get("minMustPayLecturesNumber").toString());
                    if (minMustPayLecturesNumber >= groupe.getInitialLecturesNumber()) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
                    }
                    groupe.setMinMustPayLecturesNumber(minMustPayLecturesNumber);
                }
                return ResponseEntity.ok(groupeRepository.save(groupe));
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }





    @PreAuthorize("hasRole('ROLE_TEACHER')")

    @DeleteMapping("/groupes/{id}")
    public ResponseEntity<String> deleteGroupe(@PathVariable("id") Long idGroupe) {
        if (groupeRepository.existsById(idGroupe)) {
            try {
                groupeRepository.deleteById(idGroupe);
                return ResponseEntity.ok("Le groupe a été supprimé.");
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Impossible de supprimer le groupe. Assurez-vous qu'il n'y a pas de dépendances.");
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER') or hasRole('ROLE_STUDENT')")
    @GetMapping("/search-groupes")
    public ResponseEntity<Page<Groupe>> searchGroupes(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(required = false) String specialty,
            @RequestHeader("Authorization") String authorizationHeader) {

        // Fetch groupes from the repository
        List<Groupe> groupes = groupeRepository.findGroupesByStatus(GroupeStatus.ACTIVE);

        // Filter groupes based on the query
        if (query != null && !query.isEmpty()) {
            groupes = groupes.stream()
                    .filter(groupe -> groupe.getName().toLowerCase().contains(query.toLowerCase()) ||
                            (groupe.getModule() != null && groupe.getModule().getName().toString().toLowerCase().contains(query.toLowerCase())))
                    .collect(Collectors.toList());
        }

        // Filter groupes based on the specialty if it is not null or empty
        if (specialty != null && !specialty.isEmpty()) {
            groupes = groupes.stream()
                    .filter(groupe -> groupe.getModule() != null &&
                            specialty.equalsIgnoreCase(groupe.getModule().getSpeciality().getName().toString()))
                    .collect(Collectors.toList());
        }

        // Paginate the filtered groupes
        int start = Math.min(page * size, groupes.size());
        int end = Math.min((page * size) + size, groupes.size());
        List<Groupe> paginatedGroupes = groupes.subList(start, end);

        // Fetch and set the teacher data
        for (Groupe groupe : paginatedGroupes) {
            if (groupe.getIdTeacher() != null) {
                User teacher = userProxy.getTeacher(groupe.getIdTeacher(), "tocours", authorizationHeader);
                groupe.setTeacher(teacher);
            }
        }

        // Create pageable object
        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));

        // Create Page object
        Page<Groupe> groupePage = new PageImpl<>(paginatedGroupes, pageable, groupes.size());

        // Return response entity
        return ResponseEntity.ok(groupePage);
    }


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/get-pending-groupes")
    public List<Groupe> getPendingGroupes(@RequestHeader("Authorization") String authorizationHeader) {
        List<Groupe> groupes = new ArrayList<>();

        // Assuming you have access to GroupeRepository and UserProxy instances

        List<Groupe> pendingGroupes = groupeRepository.findGroupesByStatus(GroupeStatus.PENDING);
        for (Groupe groupe : pendingGroupes) {
            Long idTeacher = groupe.getIdTeacher();
            User teacher = userProxy.getTeacher(idTeacher, "tocours", authorizationHeader);
            groupe.setTeacher(teacher);
            groupes.add(groupe);
        }

        return groupes;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping("/change-groupe-status/{id}")
    public ResponseEntity<?> changeStatus(@PathVariable(value = "id") Long idGroupe,
                                          @RequestBody Map<String, Object> payload) {
        Optional<Groupe> groupeOpt = groupeRepository.findById(idGroupe);

        if (groupeOpt.isPresent()) {
            Groupe groupe = groupeOpt.get();

            if (payload.containsKey("status") && payload.get("status") != null) {
                String status = payload.get("status").toString();
                if ("active".equalsIgnoreCase(status)) {
                    statisticsProxy.addGroupe(groupe.getIdGroupe(),groupe.getName(),groupe.getIdTeacher());
                    for (Lecture lecture: groupe.getLectures()
                    ) {
                        Long id = lecture.getIdLecture();
                        Long groupeId = lecture.getGroupe().getIdGroupe();
                        statisticsProxy.addLecture(id,lecture.getTitle(),lecture.getGroupe().getLecturePrice(),groupeId);

                    }
                    groupe.setStatus(GroupeStatus.ACTIVE);
                } else if ("inactive".equalsIgnoreCase(status)) {
                    // Delete the lectures associated with the group
                    lectureRepository.deleteAll(groupe.getLectures());
                    groupe.setLectures(Collections.emptyList());
                    groupe.setStatus(GroupeStatus.INACTIVE);
                } else {
                    return ResponseEntity.badRequest().body("Invalid status value");
                }

                Groupe updatedGroupe = groupeRepository.save(groupe);
                return ResponseEntity.ok(updatedGroupe);
            } else {
                return ResponseEntity.badRequest().body("Status is required");
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}







