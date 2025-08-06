package com.example.service.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
// @JsonIgnoreProperties(ignoreUnknown = true)
public class Vulns {

    private List<Query> queries;

    @Data
    // @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Query {
        private List<Vulnerability> vulns;
    }

    @Data
    // @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Vulnerability {
        private String id;
        private String summary;
    }
}
