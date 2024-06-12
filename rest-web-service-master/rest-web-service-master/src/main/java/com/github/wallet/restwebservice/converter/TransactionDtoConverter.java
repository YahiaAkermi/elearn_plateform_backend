package com.github.wallet.restwebservice.converter;

import com.github.wallet.restwebservice.models.*;
import com.github.wallet.restwebservice.proxy.CoursProxy;
import com.github.wallet.restwebservice.proxy.UserProxy;
import com.github.wallet.restwebservice.service.WalletService;
import com.github.wallet.restwebservice.service.contracts.IWalletService;
import com.github.wallet.restwebservice.service.models.TransactionDTO;
import com.github.wallet.restwebservice.service.models.TransactionDTODisplay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TransactionDtoConverter implements Converter<Transaction, TransactionDTO> {

    @Autowired
    CoursProxy coursProxy;
    @Autowired
    UserProxy userProxy;
    @Autowired
    private IWalletService walletService;

    @Override
    public TransactionDTO convert(Transaction transaction) {

        return TransactionDTO.builder()
                /*     .globalId(transaction.getGlobalId())*/
                .walletId(transaction.getWallet().getId())
                .amount(transaction.getAmount())
                .typeId(transaction.getType().getId())
                .lectureIds(transaction.getLectureIds())
                .teacherId(transaction.getTeacherId())
                .groupId(transaction.getGroupId())
                .lastUpdated(transaction.getLastUpdated())
                .build();
    }


}
