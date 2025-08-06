package com.example.service.demo.Repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.service.demo.dto.entity.depen;

public interface depenRepo extends JpaRepository<depen, Long> {
    
}
