package com.esi.mscours.msauth;

import com.esi.mscours.msauth.dao.*;
import com.esi.mscours.msauth.entities.*;
import com.esi.mscours.msauth.dao.AuthDao;
import com.esi.mscours.msauth.dao.RoleDao;
import com.esi.mscours.msauth.dao.StudentDao;
import com.esi.mscours.msauth.dao.TeacherDao;
import com.esi.mscours.msauth.entities.Auth;
import com.esi.mscours.msauth.entities.Role;
import com.esi.mscours.msauth.entities.Student;
import com.esi.mscours.msauth.entities.Teacher;
import com.esi.mscours.msauth.proxy.StatisticsProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
public class MsAuthApplication implements CommandLineRunner {

    @Autowired
    private AuthDao authDao;
    @Autowired
    private RoleDao roleDao;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    AdminDao adminDao;
    @Autowired
    StudentDao studentDao;
    @Autowired
    TeacherDao teacherDao;

    @Autowired
    StatisticsProxy statisticsProxy;
    public static void main(String[] args) {
        SpringApplication.run(MsAuthApplication.class, args);
    }
    @Override
    public void run(String... args) throws Exception {



        roleDao.save(new Role(1L,"ADMIN"));
        roleDao.save(new Role(2L,"TEACHER"));
        roleDao.save(new Role(3L,"STUDENT"));
        List<Role> roles = new ArrayList<>();
        roles.add(roleDao.findByName("ADMIN"));
        roles.add(roleDao.findByName("TEACHER"));
        roles.add(roleDao.findByName("STUDENT"));
        List<Role> roles1 = new ArrayList<>();
        roles1.add(roleDao.findByName("TEACHER"));
        List<Role> roles2 = new ArrayList<>();
        roles2.add(roleDao.findByName("STUDENT"));




        Auth admin= new Auth();
        admin.setId(1L);
        admin.setPassword(passwordEncoder.encode("12345678"));
        admin.setEmail("abdelhadi.merzoug@gmail.com");
        admin.setStatus(true);
        admin.setRoles(roles);
        authDao.save(admin);
        Admin admin1=new Admin();
        admin1.setId(admin.getId());
        admin1.setEmail(admin.getEmail());
        admin1.setLastName("merzoug");
        admin1.setFirstName("abdelhadi");
        admin1.setStatus(true);
        adminDao.save(admin1);




        Auth teacher= new Auth();
        teacher.setId(2L);
        teacher.setPassword(passwordEncoder.encode("12345678"));
        teacher.setEmail("fateh.benlhadj@gmail.com");
        teacher.setStatus(true);
        teacher.setRoles(roles1);
        authDao.save(teacher);

        statisticsProxy.addTeacher(2L);
        Auth user= new Auth();
        user.setId(3L);
        user.setPassword(passwordEncoder.encode("12345678"));
        user.setEmail("yahia.akermi@gmail.com");

        user.setStatus(true);
        user.setRoles(roles2);
        authDao.save(user);


        Auth user1= new Auth();
        user1.setId(4L);
        user1.setPassword(passwordEncoder.encode("12345678"));
        user1.setEmail("yahia.said@gmail.com");
        user1.setStatus(true);
        user1.setRoles(roles2);
        authDao.save(user1);


        Student student = new Student();
        student.setId(user.getId());
        student.setEmail("yahia.akermi@gmail.com");
        student.setFirstName("Akermi");
        student.setLastName("Yahia");
        student.setBirthdate(new Date());
        student.setGender("Male");
        student.setStatus(true);
        student.setSpeciality("Science_3");
        student.setIdWallet(1L);


        Student student1 = new Student();
        student1.setId(user1.getId());
        student1.setEmail("yahia.said@gmail.com");
        student1.setFirstName("Said");
        student1.setLastName("Yahia");
        student1.setBirthdate(new Date());
        student1.setGender("Male");
        student1.setStatus(true);
        student1.setSpeciality("Science_3");
        student1.setIdWallet(2L);

        studentDao.save(student);
        studentDao.save(student1);

        Teacher teacherr = new Teacher();
        teacherr.setId(teacher.getId());
        teacherr.setEmail("fateh.benlhadj@gmail.com");
        teacherr.setFirstName("Benlhadj");
        teacherr.setLastName("mohamed");
        teacherr.setGender("Male");
        teacherr.setStatus(true);
        teacherr.setModuleName("ARABIC");
        teacherr.setBirthdate(new Date());
        teacherr.setCv("https://drive.google.com/file/d/1nCQuAxf2O_xdWMrlpuVem_tF8aTRVFra/view");
        teacherDao.save(teacherr);




    }
}
