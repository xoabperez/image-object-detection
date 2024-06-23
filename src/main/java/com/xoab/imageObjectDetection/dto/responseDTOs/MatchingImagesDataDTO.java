package com.xoab.imageObjectDetection.dto.responseDTOs;

import lombok.Data;

@Data
public class MatchingImagesDataDTO {
    int id;
    String url;
    // Double confidence;

    // So ObjectMapper can readValue of this class
    public MatchingImagesDataDTO() {}

    public MatchingImagesDataDTO(String url, int id) {
        this.url = url;
        this.id = id;
    }
}
