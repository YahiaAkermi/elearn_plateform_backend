package com.github.wallet.restwebservice.service.models;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Data
@Setter
@Getter
@Builder
public class TransactionDTO {

    private int typeId;
    private double amount;
    private long walletId;
    private List<Long> lectureIds = new ArrayList<>(); // Ensure non-null initialization
    private Long teacherId;
    private Long groupId;
    private Date lastUpdated;

    public TransactionDTO(){ }

    public TransactionDTO(int typeId, double amount, long walletId, List<Long> lectureIds, Long teacherId, Long groupId, Date lastUpdated) {
        this.typeId = typeId;
        this.amount = amount;
        this.walletId = walletId;
        this.lectureIds = lectureIds != null ? lectureIds : new ArrayList<>(); // Ensure non-null assignment
        this.teacherId = teacherId;
        this.groupId = groupId;
        this.lastUpdated = lastUpdated;
    }
}