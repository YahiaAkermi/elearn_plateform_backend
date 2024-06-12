package com.github.wallet.restwebservice.proxy;

import com.github.wallet.restwebservice.models.User;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "ms-auth")
@LoadBalancerClient(name = "ms-auth")
public interface UserProxy {

    @GetMapping("/students/search/findStudentById")
    User getStudent(@RequestParam("id") Long id,
                  @RequestParam("projection") String projection, @RequestHeader("Authorization") String authorizationHeader);
    @GetMapping("/teachers/search/findTeacherById")
    User getTeacher(@RequestParam("id") Long id,
                    @RequestParam("projection") String projection, @RequestHeader("Authorization") String authorizationHeader);
}
