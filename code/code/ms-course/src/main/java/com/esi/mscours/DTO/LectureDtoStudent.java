package com.esi.mscours.DTO;

import com.esi.mscours.entities.Document;
import com.esi.mscours.entities.Groupe;
import com.esi.mscours.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LectureDtoStudent {
    private Long idLecture;
    private String date;
    private String title;
    private List<Document> docs;
    private String groupeName;
    private User teacher;
    private int roomId;
    private boolean paymentStatus;
}
