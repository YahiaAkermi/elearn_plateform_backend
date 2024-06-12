package com.esi.mscours.proxy;

import com.esi.mscours.model.User;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.hateoas.CollectionModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "ms-auth")
@LoadBalancerClient(name = "ms-auth")
public interface UserProxy {

    @GetMapping("/teachers/search/findTeacherById")
    public User getTeacher(@RequestParam("id") Long id,
                                              @RequestParam("projection") String projection,@RequestHeader("Authorization") String authorizationHeader);

}
