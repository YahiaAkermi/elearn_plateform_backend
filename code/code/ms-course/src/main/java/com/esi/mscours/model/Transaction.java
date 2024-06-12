package com.esi.mscours.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Transaction {

    private int typeId;
    private double amount;
    private long walletId;
    private Long lectureId;
    private Date lastUpdated;



}
