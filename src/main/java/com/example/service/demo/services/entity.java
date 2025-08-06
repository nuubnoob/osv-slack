package com.example.service.demo.services;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.annotation.Generated;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "json_file_data")
public class entity {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    public Integer id;

    @Column(name = "name", nullable = false)
    public String name;

    @Column(name = "file", nullable = false, columnDefinition = "TEXT")
    public String file;

}
