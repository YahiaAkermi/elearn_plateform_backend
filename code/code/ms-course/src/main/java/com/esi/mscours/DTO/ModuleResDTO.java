package com.esi.mscours.DTO;

import com.esi.mscours.entities.ModuleName;
import com.esi.mscours.entities.SpecialityName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModuleResDTO {
    private Long idModule;
    private ModuleName name;
    private SpecialityName specialityName;

    // Constructors, getters, and setters


}
