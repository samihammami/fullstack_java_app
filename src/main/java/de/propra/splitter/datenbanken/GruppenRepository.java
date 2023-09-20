package de.propra.splitter.datenbanken;


import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface GruppenRepository extends CrudRepository<GruppeEntity, Integer> {
    List<GruppeEntity> findAll();

    GruppeEntity findById(int id);

}
