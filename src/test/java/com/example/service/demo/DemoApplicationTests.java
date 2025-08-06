package com.example.service.demo;

import com.example.service.demo.dto.Request;
import com.example.service.demo.services.OsvScanner;
import com.example.service.demo.services.slackPush;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class DemoApplicationTests {


    @Autowired
    OsvScanner osvScanner;


    @Autowired
    slackPush sp;

    @Autowired
    KafkaTemplate<String,String> kafkaTemplate;

    @BeforeEach
    public void before(){
        osvScanner.seenPackages.put("a-0","vuln");
        osvScanner.seenPackages.put("b-0","vuln");
    }

    @Test
    public void filterTest(){
        Map<String,String> dependencies = new HashMap<>();
        dependencies.put("a","0");
        dependencies.put("c","0");


        Map<String,String> filterDependencies = osvScanner.filterDependencies(dependencies);
        assertFalse(filterDependencies.containsKey("a"));
        assertTrue(filterDependencies.containsKey("c"));

    }


    @Test
    public void sendMessage(){
        assertDoesNotThrow(() -> kafkaTemplate.send("slack-messages-queue", "test-message"));
    }

    @AfterEach
    public void cleanUp(){
        osvScanner.seenPackages.remove("a-0","vuln");
        osvScanner.seenPackages.remove("b-0","vuln");
    }



}
