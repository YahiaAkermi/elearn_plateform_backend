package com.esi.mscours.DTO;

import com.esi.mscours.entities.Module;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupeDTO {
    private String name;
    private Double lecturePrice;
    private int max;
    private Long idModule;
    private Module module;
    private Long idUser;
    private String image;
    private String lectureDay;
    private int initialLecturesNumber;
    private int minMustPayLecturesNumber;
    private List<LectureDTO> lectures; // Add this field to hold lecture details
}
