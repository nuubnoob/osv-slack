package com.example.service.demo.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Response {
    
    @JsonProperty("package")
    private String Package;
    private String version;
    
    @JsonProperty("vulns")
    private List<vulnData> vulns;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class vulnData{
        private String id;
        private String summary;
    }
}
