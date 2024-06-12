package com.github.wallet.restwebservice.proxy;

import com.github.wallet.restwebservice.models.Groupe;
import com.github.wallet.restwebservice.models.Lecture;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "ms-cours")
@LoadBalancerClient(name = "ms-cours")
public interface CoursProxy {
    @GetMapping("/groupes/search/findGroupeByIdGroupe")
    Groupe getGroup(@RequestParam("idGroupe") Long idGroupe,
                    @RequestParam("projection") String projection,@RequestHeader("Authorization") String authorizationHeader);
    @GetMapping("/lectures/search/findLectureByIdLecture")
    Lecture getLecture(@RequestParam("idLecture") Long idLecture,
                       @RequestParam("projection") String projection, @RequestHeader("Authorization") String authorizationHeader);
}
