package org.demo.appmockito.ejemplos.repositories;

import org.demo.appmockito.ejemplos.models.Examen;

import java.util.List;

public interface ExamenRepository {

    List<Examen> findAll();

    Examen guardar(Examen examen);
}
