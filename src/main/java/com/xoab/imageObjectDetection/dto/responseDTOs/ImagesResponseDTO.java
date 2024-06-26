package com.xoab.imageObjectDetection.dto.responseDTOs;

import lombok.Data;

import java.util.List;

@Data
public class ImagesResponseDTO {
    private List<ImageDataDTO> images;

    public ImagesResponseDTO(List<ImageDataDTO> images) {
        this.images = images;
    }
}
