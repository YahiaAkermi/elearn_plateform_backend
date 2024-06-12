package com.esi.mscours.msauth.proxy;

import com.esi.mscours.msauth.DTO.WalletDTO;
import com.esi.mscours.msauth.model.Wallet;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name="ms-payment")
@LoadBalancerClient(name = "ms-payment")
public interface PaymentProxy {

    @PostMapping("/api/v1/user/{userId}/wallet/create")
    ResponseEntity<?> createWallet(@PathVariable("userId") long userId, @RequestBody double balance);
}
