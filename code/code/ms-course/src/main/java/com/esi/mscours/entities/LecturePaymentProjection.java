package com.esi.mscours.entities;


import org.springframework.data.rest.core.config.Projection;

import java.util.Date;

@Projection(name = "topayment", types= Lecture.class)

public interface LecturePaymentProjection {
     Long getIdLecture();
     String getTitle();
}
