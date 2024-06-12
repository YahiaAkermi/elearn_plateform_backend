package com.esi.mscours.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class LectureDTO {

    private String date;
    private String title;
    private Long idGroupe;
    private Long idTeacher;
    private List<Long> docs;

}
