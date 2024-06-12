package com.esi.mscours.DTO;

import com.esi.mscours.entities.SpecialityName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpecialityDTO {

    private Long idSpeciality;
    private SpecialityName name;
}
