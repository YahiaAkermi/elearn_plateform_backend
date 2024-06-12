package com.esi.mscours.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Debited {

    private Long idStudent ;
    private Long idLecture;
    private Date date ;

    private int lecturePrice ;
}
