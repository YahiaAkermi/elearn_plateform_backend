package com.github.wallet.restwebservice.service.models;

import com.github.wallet.restwebservice.models.Groupe;
import com.github.wallet.restwebservice.models.Lecture;
import com.github.wallet.restwebservice.models.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTODisplay {
    private int typeId;
    private double amount;

    private List<Lecture> lectures = new ArrayList<>(); // Ensure non-null initialization
    private User user;
    private Groupe group;
    private Date lastUpdated;
}
