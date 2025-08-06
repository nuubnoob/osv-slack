package com.example.service.demo.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import com.example.service.demo.dto.Vulns;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.CurrentTimestamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.service.demo.Repo.FileRepo;
import com.example.service.demo.Repo.depenRepo;
import com.example.service.demo.dto.PackageDto;
import com.example.service.demo.dto.Request;
import com.example.service.demo.dto.Response;
import com.example.service.demo.dto.Response.vulnData;
import com.example.service.demo.dto.entity.depen;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.support.JstlUtils;


@Component
@Slf4j
public class OsvScanner {
    
    public Map<String, String> seenPackages = new HashMap<>();
    
    public static final String osvUrl = "https://api.osv.dev/v1/query";

    public static final String topic = "";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    FileRepo repo;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    depenRepo depenRDepen;

    public final ObjectMapper objectMapper = new ObjectMapper();

    @Scheduled(cron = "* * * * * *")
    public void fileScanner(){
        try{
            List<Request> scanList = new ArrayList<>();
            entity latestdata = repo.latestEntity().get();
            PackageDto data = objectMapper.readValue(latestdata.getFile(),PackageDto.class);
            Map<String,String> dependenciesMap = data.getDependencies();
            
            if(seenPackages.isEmpty()){
                fetchSeenDependencies();
            }

            Map<String,String> filteredMap = filterDependencies(dependenciesMap);

            for(String key :filteredMap.keySet()){
                Request.Package pck = new Request.Package();
                Request reqObj = new Request();
                pck.setName(key);
                reqObj.setVersion(filteredMap.get(key));
                reqObj.setMypackage(pck);
                scanList.add(reqObj);
            }


            for(Request req:scanList){
                String response = scanCall(req);
                if(response != null && !response.isEmpty()){
                    log.info("Response for package {}: {}", req.getMypackage().getName(), req.getVersion()+ " - " + req.getMypackage().getName());
                } else {
                    log.warn("No response received for package {}", req.getMypackage().getName());
                }
            }
            batchInsertDependencies(scanList);

            log.info("version to scan",scanList);
        }catch(Exception e){

        }


    }


    public String scanCall(Request packages){
        try {
            Date date = new Date();
            Timestamp currentTimestamp = new Timestamp(date.getTime());

            String json = objectMapper.writeValueAsString(packages);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(osvUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
     
            if(response.statusCode()!=200) {
                log.error("Failed to fetch data from OSV API, status code: {}", response.statusCode());
                //retry
                return null;
            }else{
                log.info("call to OSV scanner success");
                Response responseMessage = processResponse(response.body());;
                responseMessage.setPackage(packages.getMypackage().getName());
                responseMessage.setVersion(packages.getVersion());
                Map<String,Object> respones = new HashMap<>();
                respones.put("package",packages.getMypackage().getName());
                respones.put("version",packages.getVersion());
                if(responseMessage.getVulns() == null || responseMessage.getVulns().isEmpty()){
                    respones.put("vulns","The package is free from any \n" +
                            "vulnerabilities");
                }else{
                    respones.put("vulns",responseMessage.getVulns());
                }
                respones.put("OSV scan time",currentTimestamp);
                sendMessage(objectMapper.writeValueAsString(respones),"slack-message-topic");
            }
            return response.body();
        } catch (Exception e) {
            log.error("Error creating JSON for OSV API", e);
            return null;
        }
    }

    public Response processResponse(String response) {
        try {
            Response responseMessage = new Response();
            JsonNode root = objectMapper.readTree(response);
            if(root.has("vulns")){
                List<Response.vulnData> messageBody = objectMapper.readValue(
                        root.get("vulns").toString(),
                        new TypeReference<List<vulnData>>() {}
                );
                log.info("data : {}",messageBody);
                responseMessage.setVulns(messageBody);
            }
            return responseMessage;
        } catch (Exception e) {
            log.error("Error processing response", e);
            return null;
        }
    }




    public void sendMessage(String message, String topicName) throws Exception{
        log.info("Sending : {}", message);
        try{
            SendResult<String, String> status = kafkaTemplate.send("slack-messages-queue", message).get();
            log.info("message sent successfully");
        }catch (Exception e){
            log.error("faced error while sending message");
        }
    }

    public Map<String,String> filterDependencies(Map<String,String> dependenciesMap){
            List<Request> scanList = new ArrayList<>();
            Map<String,String> filteredMap = new HashMap<>();
            Request reqObj = new Request();
            for(Entry<String, String> keyEntry:dependenciesMap.entrySet()){
                    String key = keyEntry.getKey() + "-"+keyEntry.getValue();
                    if(! seenPackages.containsKey(key)){
                        filteredMap.put(keyEntry.getKey(),keyEntry.getValue());
                    }
            }
            return filteredMap;
    }

    public void fetchSeenDependencies(){
            try{
                    String query = 
                """
                select sd.key, sd.value from seen_dependencies sd;
                """;

            jdbcTemplate.queryForList(query).forEach(row -> {
                seenPackages.put(row.get("key").toString(), row.get("value").toString());
            });

        }catch(Exception e){
            
        }

    }


    public void batchInsertDependencies(List<Request> scanList) {
        List<depen> batch = new ArrayList<>(); 
        for(Request req : scanList) {
            depen dependency = new depen();
            String key = req.getMypackage().getName() + "-" + req.getVersion();
            if(seenPackages.containsKey(key)) {
                log.info("Dependency {} already exists, skipping insert", key);
                continue;
            }
            dependency.setKey(key);
            dependency.setValue("vulns");
            batch.add(dependency);
        }

        depenRDepen.saveAll(batch);

        for(depen dep : batch) {
            seenPackages.put(dep.getKey(), dep.getValue());
        }

        log.info("Batch insert of dependencies completed, total: {}", batch.size());
    }

    

}
