package com.github.wallet.restwebservice.proxy;

import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "ms-statistics")
@LoadBalancerClient(name = "ms-statistics")
public interface StatisticsProxy {
    @PostMapping("/api/v1/stat/addPayment/{idPayment}")
    ResponseEntity<?> addPayment(@PathVariable Long idPayment, @RequestParam Long idLecture);
}
