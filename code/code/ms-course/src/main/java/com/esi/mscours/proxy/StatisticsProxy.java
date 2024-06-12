package com.esi.mscours.proxy;

import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name="ms-statistics")
@LoadBalancerClient(name = "ms-statistics")
public interface StatisticsProxy {

    @PostMapping("/api/v1/stat/addGroupe/{idGroupe}")
    ResponseEntity<?> addGroupe(@PathVariable Long idGroupe, @RequestParam String name, @RequestParam Long idTeacher);

    @PostMapping("/api/v1/stat/addLecture/{idLecture}")
    ResponseEntity<?> addLecture(@PathVariable Long idLecture, @RequestParam String name, @RequestParam double lecturePrice, @RequestParam Long idGroupe);

    @PostMapping("/api/v1/stat/addStudent/{idStudent}")
    ResponseEntity<?> addStudent(@PathVariable Long idStudent, @RequestParam Long idGroupe);

    @PostMapping("/api/v1/stat/addPayment/{idPayment}")
    ResponseEntity<?> addPayment(@PathVariable Long idPayment, @RequestParam Long idLecture);


    @DeleteMapping("/api/v1/stat/deleteGroupe/{idGroupe}")
    public ResponseEntity<?> deleteGroupe(@PathVariable Long idGroupe);

    @DeleteMapping("/api/v1/stat/deleteStudentFromGroup/{idStudent}")
    public ResponseEntity<?> deleteStudentFromGroup(@PathVariable Long idStudent, @RequestParam Long idGroupe);


}