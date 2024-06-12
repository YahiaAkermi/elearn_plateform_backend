package com.esi.mscours.msauth.proxy;




import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;


@FeignClient(name="ms-statistics")
@LoadBalancerClient(name = "ms-statistics")
public interface StatisticsProxy {

    @PostMapping("/api/v1/stat/addTeacher/{idTeacher}")
    ResponseEntity<?> addTeacher(@PathVariable Long idTeacher);

    @DeleteMapping("/api/v1/stat/deleteTeacher/{idTeacher}")
    public ResponseEntity<?> deleteTeacher(@PathVariable Long idTeacher);




}