package com.xoab.imageObjectDetection.dto.responseDTOs;

import com.xoab.imageObjectDetection.dto.ImageRequestDTO;
import lombok.Data;

@Data
public class ImageDataDTO {
    int id;
    String label;
    String url;
    byte[] imageData;
    boolean objectDetectionEnabled;
    String objects;
    // Double[] confidences;

    public ImageDataDTO(){}

    public ImageDataDTO(ImageRequestDTO imageRequestDTO, byte[] imageData, String url, int id, String objects) {

        this.label = imageRequestDTO.getImageLabel();
        this.objectDetectionEnabled = imageRequestDTO.isEnableObjectDetection();
        this.imageData = imageData;
        this.url = url;
        this.id = id;
        this.objects = objects;
    }
}
