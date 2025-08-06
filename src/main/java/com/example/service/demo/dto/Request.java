package com.example.service.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Request {
    @JsonProperty("package") 
    private Package mypackage;
    private String version;

    @Data
    public static class Package{
        private String name;
    }
}
