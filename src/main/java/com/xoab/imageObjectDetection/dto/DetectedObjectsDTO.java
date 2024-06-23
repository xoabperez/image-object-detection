package com.xoab.imageObjectDetection.dto;

import lombok.Data;

@Data
public class DetectedObjectsDTO {
    private String name;
    private Double confidence;

    public DetectedObjectsDTO(){}

    public DetectedObjectsDTO(String name, Double confidence) {
        this.name = name;
        this.confidence = confidence;
    }
}
