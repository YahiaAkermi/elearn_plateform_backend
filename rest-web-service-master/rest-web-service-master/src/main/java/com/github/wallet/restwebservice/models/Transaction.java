package com.github.wallet.restwebservice.models;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Getter
@Setter
@Entity
@Table(name = "wallet_transaction")
@EntityListeners(AuditingEntityListener.class)
public class Transaction {

    @Id
    @Column(name = "id")
    @NotNull
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id")
    private Type type;

    @ElementCollection
    @CollectionTable(name = "transaction_lecture", joinColumns = @JoinColumn(name = "transaction_id"))
    @Column(name = "lecture_id")
    private List<Long> lectureIds = new ArrayList<>(); // Ensure non-null initialization

    @Column(name = "teacher_id")
    private Long teacherId;

    @Column(name = "group_id")
    private Long groupId;

    @NotNull
    @Column(name = "amount")
    private double amount;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;

    @Column(name = "last_updated")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdated;

    public Transaction(){ }

    public Transaction(Type type, double amount, Wallet wallet, List<Long> lectureIds, Long teacherId, Long groupId) {
        this.type = type;
        this.amount = amount;
        this.wallet = wallet;
        this.lectureIds = lectureIds != null ? lectureIds : new ArrayList<>(); // Ensure non-null assignment
        this.teacherId = teacherId;
        this.groupId = groupId;
        this.lastUpdated = new Date();
    }
}

