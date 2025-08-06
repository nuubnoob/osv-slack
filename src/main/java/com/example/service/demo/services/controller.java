package com.example.service.demo.services;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.service.demo.Repo.FileRepo;
import com.example.service.demo.dto.PackageDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/file")
public class controller {

    @Autowired
    public FileRepo fileRepo;

    @Autowired
    public ObjectMapper objectMapper;

    @PostMapping("/uploadFile")
    public String saveFile(@RequestParam("file") MultipartFile file, @RequestParam String name) {
        try{
            String data = new String(file.getBytes(), StandardCharsets.UTF_8);
            JsonNode newData = objectMapper.readTree(data);
            entity packageEntity = new entity();
            packageEntity.setFile(data);
            packageEntity.setName("package.json");
            fileRepo.save(packageEntity);
            return "file uploaded successfully";
        }catch(Exception e){
            e.printStackTrace();
            return "error";
        }
    }





}
