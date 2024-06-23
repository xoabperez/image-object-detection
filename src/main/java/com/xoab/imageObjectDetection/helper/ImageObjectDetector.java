package com.xoab.imageObjectDetection.helper;

import com.xoab.imageObjectDetection.dto.DetectedObjectsDTO;
import com.xoab.imageObjectDetection.dto.ImageRequestDTO;

public interface ImageObjectDetector {

    // Given an image request, return a list of objects detected and confidence values
    DetectedObjectsDTO[] detectObjects(ImageRequestDTO imageRequestDTO);

}
