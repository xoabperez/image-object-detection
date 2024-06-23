package com.xoab.imageObjectDetection.dto.responseDTOs;

import com.xoab.imageObjectDetection.dto.ImageRequestDTO;
import lombok.Data;

@Data
public class AllImagesDataDTO {
    int id;
    String label;
    String url;
    boolean objectDetectionEnabled;
    String objects;
    // Double[] confidences;

    // So ObjectMapper can readValue of this class
    public AllImagesDataDTO() {}

    public AllImagesDataDTO(ImageRequestDTO imageRequestDTO, String url, int id, String objects) {

        this.label = imageRequestDTO.getImageLabel();
        this.objectDetectionEnabled = imageRequestDTO.isEnableObjectDetection();
        this.url = url;
        this.id = id;
        this.objects = objects;
    }
}
