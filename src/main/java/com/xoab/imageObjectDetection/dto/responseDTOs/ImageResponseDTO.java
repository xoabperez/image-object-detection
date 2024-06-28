package com.xoab.imageObjectDetection.dto.responseDTOs;

import lombok.Data;

@Data
public class ImageResponseDTO {
    private ImageDataDTO image;

    public ImageResponseDTO() {};

    public ImageResponseDTO(ImageDataDTO image) {
        this.image = image;
    }
}
