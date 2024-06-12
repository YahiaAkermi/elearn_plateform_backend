package com.esi.mscours.repository;

import com.esi.mscours.entities.Module;
import com.esi.mscours.entities.ModuleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;


import java.util.List;

@RepositoryRestResource
public interface ModuleRepository extends JpaRepository<Module, Long> {
    List<Module> findModulesByName(ModuleName name);

}
