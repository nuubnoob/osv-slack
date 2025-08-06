package com.example.service.demo.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PackageDto {
    String name;
    String version;
    String Description;
    @JsonProperty("dependencies")
    Map<String,String> dependencies;
}




