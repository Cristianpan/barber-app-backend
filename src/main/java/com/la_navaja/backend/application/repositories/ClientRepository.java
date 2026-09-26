package com.la_navaja.backend.application.repositories;

import java.util.List;
import java.util.Optional;

import com.la_navaja.backend.domain.models.Client;

public interface ClientRepository {

    Client save(Client client);

    Optional<Client> findById(Long id);

    List<Client> findAll();

    void deleteById(Long id);

    boolean existsById(Long id);
}
