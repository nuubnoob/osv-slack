package com.example.service.demo.Repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import com.example.service.demo.services.entity;

public interface FileRepo extends JpaRepository<entity, Long> {
    Optional<entity> findByName(String name);

    @Query(value = "select * from json_file_data e order by uploaded_at desc limit 1",nativeQuery = true)
    Optional<entity> latestEntity();

}
