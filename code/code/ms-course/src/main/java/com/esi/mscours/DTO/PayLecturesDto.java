package com.esi.mscours.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayLecturesDto {
    private int typeId;
    private double amount;
    private long walletId;
    private List<Long> lectureIds;
    private Long teacherId;
    private Long groupId;
}
