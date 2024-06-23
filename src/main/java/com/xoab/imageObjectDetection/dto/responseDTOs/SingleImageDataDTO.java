package com.xoab.imageObjectDetection.dto.responseDTOs;

import com.xoab.imageObjectDetection.dto.ImageRequestDTO;
import lombok.Data;

@Data
public class SingleImageDataDTO {
    int id;
    String label;
    byte[] imageData;
    String url;
    boolean objectDetectionEnabled;
    String objects;
    // Double[] confidences;

    // So ObjectMapper can readValue of this class
    public SingleImageDataDTO() {}

    public SingleImageDataDTO(ImageRequestDTO imageRequestDTO, byte[] imageData, String url, int id, String objects) {

        this.label = imageRequestDTO.getImageLabel();
        this.objectDetectionEnabled = imageRequestDTO.isEnableObjectDetection();
        this.imageData = imageData;
        this.url = url;
        this.id = id;
        this.objects = objects;
    }
}
