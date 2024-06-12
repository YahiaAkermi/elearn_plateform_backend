package com.esi.mscours.msauth.controller;


import com.esi.mscours.msauth.DTO.*;
import com.esi.mscours.msauth.config.TokenProvider;
import com.esi.mscours.msauth.dao.*;
import com.esi.mscours.msauth.entities.*;

import com.esi.mscours.msauth.model.Wallet;
import com.esi.mscours.msauth.proxy.PaymentProxy;
import com.esi.mscours.msauth.proxy.StatisticsProxy;
import com.esi.mscours.msauth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.List;


@RequestMapping("/users")
@RestController
public class UserController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenProvider jwtTokenUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthDao userDao;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RoleDao roleDao;

    @Autowired
    TokenProvider tokenProvider;

    @Autowired
    private StudentDao studentDao;
    @Autowired
    private TeacherDao teacherDao;
    @Autowired
    private AdminDao adminDao;
    @Autowired
    private PaymentProxy paymentProxy;
    @Autowired
    private StatisticsProxy statisticsProxy;


    @Resource(name = "userService")
    private UserDetailsService userDetailsService;

    //Reset the password

    @RequestMapping(value = "/reset/{email}" ,method = RequestMethod.PUT)
    public  ResponseEntity<?> resetPassword(@PathVariable String email,@RequestBody Reset reset){
        Auth auth =  userDao.findByEmail(email);
        if(auth != null){
            auth.setPassword(passwordEncoder.encode((reset.getPassword())));
            userDao.save(auth);
            return  new ResponseEntity<>("Le mot de passe est bien modifié",HttpStatus.OK);
        }
        return  new ResponseEntity<>("Utilisateur n'existe pas",HttpStatus.BAD_REQUEST);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/student/{email}",method = RequestMethod.GET)
    public  ResponseEntity<?> getStudent (@PathVariable String email, @RequestHeader("Authorization") String authorizationHeader){

        Student student =studentDao.findStudentByEmail(email);
        return   ResponseEntity.ok().header(HttpHeaders.AUTHORIZATION,authorizationHeader).body(student);



    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/teacher/{email}",method = RequestMethod.GET)
    public  ResponseEntity<?> getTeacher (@PathVariable String email, @RequestHeader("Authorization") String authorizationHeader){

        Teacher teacher = teacherDao.findTeacherByEmail(email);
        return   ResponseEntity.ok().header(HttpHeaders.AUTHORIZATION,authorizationHeader).body(email);



    }



    /// KHASNNIII NSAGAD AUTHENTIFICATION TA3 TEACHER

    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public ResponseEntity<?> generateToken(@RequestBody LoginUser loginUser) throws AuthenticationException {



        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginUser.getEmail(),
                        loginUser.getPassword()
                )
        );

        Auth  user = userDao.findAuthByEmail(loginUser.getEmail());
        if(user.isStatus()==true) {

            if (authentication.isAuthenticated()) {
                SecurityContextHolder.getContext().setAuthentication(authentication);
                final String token = jwtTokenUtil.generateToken(authentication);

                return ResponseEntity.ok().header(HttpHeaders.AUTHORIZATION, token).body(token);
            } else {

                throw new RuntimeException("invalid access");
            }
        }
        else return new ResponseEntity<>("le compte est desctiver",HttpStatus.BAD_REQUEST);

    }




    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/profile-student/{email}")
    public Student getProfileStudent(@PathVariable("email") String email){
        Auth user=userDao.findByEmail(email);
        Student result= studentDao.findStudentByEmail(email);
        Student student=new Student();
        student.setUserId(user.getId());
        student.setEmail(result.getEmail());
        student.setId(result.getId());
        student.setSpeciality(result.getSpeciality());
        student.setFirstName(result.getFirstName());
        student.setLastName(result.getLastName());
        student.setBirthdate(result.getBirthdate());
        return student;

    }

    @PreAuthorize("hasRole('TEACHER')")
    @GetMapping("/profile-teacher/{email}")
    public Teacher getProfileTeacher(@PathVariable("email") String email){
        Auth user=userDao.findByEmail(email);
        Teacher result= teacherDao.findTeacherByEmail(email);
        Teacher teacher=new Teacher();
        teacher.setUserId(user.getId());
        teacher.setEmail(result.getEmail());
        teacher.setId(result.getId());
        teacher.setModuleName(result.getModuleName());
        teacher.setFirstName(result.getFirstName());
        teacher.setLastName(result.getLastName());
        teacher.setBirthdate(result.getBirthdate());
        return teacher;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/profile-admin/{email}")
    public Admin getProfileAdmin(@PathVariable("email") String email){
        Admin result= adminDao.findAdminByEmail(email);
        Admin admin=new Admin();
        admin.setEmail(result.getEmail());
        admin.setId(result.getId());
        admin.setFirstName(result.getFirstName());
        admin.setLastName(result.getLastName());
        return admin;
    }


    @RequestMapping(value = "/register-student", method = RequestMethod.POST)
    public ResponseEntity<String> register(@RequestBody StudentDTO studentDTO) {
        if (userDao.existsByEmail(studentDTO.getEmail())) {
            return new ResponseEntity<>("L'email n'est pas disponible!", HttpStatus.BAD_REQUEST);
        }

        Auth user = new Auth();
        Student student = new Student();

        student.setEmail(studentDTO.getEmail());

        student.setFirstName(studentDTO.getFirstName());
        student.setLastName(studentDTO.getLastName());

        student.setGender(studentDTO.getGender());
        student.setStatus(true);
        student.setBirthdate(studentDTO.getBirthdate());
        student.setSpeciality(studentDTO.getSpeciality());

        user.setEmail(studentDTO.getEmail());
        user.setPassword(passwordEncoder.encode((studentDTO.getPassword())));
        List<Role> roles = new ArrayList<>();
        roles.add(roleDao.findByName("STUDENT"));
        user.setRoles(roles);
        user.setStatus(true);
        Auth user1=userDao.save(user);
        student.setId(user1.getId());
        studentDao.save(student);
        ResponseEntity<?> wallet = paymentProxy.createWallet(user1.getId(), 0.0);
        System.out.println(wallet.getBody());
        if (wallet.getStatusCode().is2xxSuccessful()){

        }
        return new ResponseEntity<>("Student est bien enregistrer!", HttpStatus.OK);

    }


    // register-teacher
    @RequestMapping(value = "/register-teacher", method = RequestMethod.POST)
    public ResponseEntity<String> registerTeacher(@RequestBody TeacherDTO teacherDTO) {
        if (userDao.existsByEmail(teacherDTO.getEmail())) {
            return new ResponseEntity<>("L'email n'est pas disponible!", HttpStatus.BAD_REQUEST);
        }

        Auth user = new Auth();
        Teacher teacher = new Teacher();
        teacher.setEmail(teacherDTO.getEmail());
        teacher.setFirstName(teacherDTO.getFirstName());
        teacher.setLastName(teacherDTO.getLastName());
        teacher.setGender(teacherDTO.getGender());
        teacher.setStatus(false);
        teacher.setBirthdate(teacherDTO.getBirthdate());
        teacher.setModuleName(teacherDTO.getModuleName());

        teacher.setCv(teacherDTO.getCv()); // Set the CV link

        user.setEmail(teacherDTO.getEmail());
        user.setPassword(passwordEncoder.encode((teacherDTO.getPassword())));

        List<Role> roles = new ArrayList<>();
        roles.add(roleDao.findByName("TEACHER"));
        user.setRoles(roles);
        Auth user1=userDao.save(user);
        teacher.setId(user1.getId());
        teacherDao.save(teacher);
        ResponseEntity<?> wallet = paymentProxy.createWallet(user1.getId(), 0.0);
        return new ResponseEntity<>("Teacher est bien enregistrer! mais n'est pas encore activé", HttpStatus.OK);
    }


    // get teachers
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/teachers")
    public List<Teacher> getProfileTeacher(){

        List<Teacher> teachers=teacherDao.findAll();
        return  teachers;
    }

    // activate teacher acount

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value="/activate-teacher/{email}", method = RequestMethod.PATCH)
    public ResponseEntity<?> activateTeacher(@PathVariable String email) {
        if (userDao.existsByEmail(email)) {
            Auth auth = userDao.findAuthByEmail(email);
            auth.setStatus(true);
            userDao.save(auth);
            Teacher teacher = teacherDao.findTeacherByEmail(email);
            teacher.setStatus(true);
            teacherDao.save(teacher);
            statisticsProxy.addTeacher(teacher.getId());
            return new ResponseEntity<>("Teacher account activated successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Teacher does not exist", HttpStatus.BAD_REQUEST);
        }
    }
}
