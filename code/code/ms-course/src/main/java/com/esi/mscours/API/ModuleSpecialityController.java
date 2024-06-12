package com.esi.mscours.API;

import com.esi.mscours.DTO.ModuleDTO;
import com.esi.mscours.DTO.ModuleResDTO;
import com.esi.mscours.DTO.SpecialityDTO;
import com.esi.mscours.entities.Module;
import com.esi.mscours.entities.ModuleName;

import com.esi.mscours.entities.Speciality;
import com.esi.mscours.repository.ModuleRepository;
import com.esi.mscours.repository.SpecialityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("api/v1")
public class ModuleSpecialityController {

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private SpecialityRepository specialityRepository;

    @GetMapping("/modules")
    public List<Module> getAllModules() {
        return moduleRepository.findAll();
    };

    @GetMapping("/specialities")
    public  List<Speciality> getAllSpecialities() {
        return specialityRepository.findAll();
    }


    @PostMapping("/specialities")
    public Speciality addSpecilality(@RequestBody SpecialityDTO specialityDTO) {

        Speciality speciality = new Speciality();
        speciality.setName(specialityDTO.getName());

        return  specialityRepository.save(speciality);
    }

    @GetMapping("/modules-by-name")
    public List<ModuleResDTO> getModulesByName(@RequestParam(value = "name") ModuleName name) {
        List<Module> modules = moduleRepository.findModulesByName(name);
        return modules.stream().map(module ->
                new ModuleResDTO(
                        module.getIdModule(),
                        module.getName(),
                        module.getSpeciality() != null ? module.getSpeciality().getName() : null // Assuming Speciality has a getName method
                )
        ).collect(Collectors.toList());
    }

}

