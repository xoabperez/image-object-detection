package com.xoab.imageObjectDetection.service;

import com.xoab.imageObjectDetection.dao.ImageDAO;
import com.xoab.imageObjectDetection.dto.DetectedObjectsDTO;
import com.xoab.imageObjectDetection.dto.ImageRequestDTO;
import com.xoab.imageObjectDetection.dto.responseDTOs.ImageDataDTO;
import com.xoab.imageObjectDetection.dto.responseDTOs.ImageResponseDTO;
import com.xoab.imageObjectDetection.dto.responseDTOs.ImagesResponseDTO;
import com.xoab.imageObjectDetection.helper.ImageObjectDetector;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ImageService {

    @Autowired
    ImageObjectDetector imageObjectDetector;

    @Autowired
    ImageDAO imageDAO;

    public ImageDataDTO addImage(ImageRequestDTO imageRequestDTO, String localFilePath){
        DetectedObjectsDTO[] detectedObjects = null;

        if (imageRequestDTO.isEnableObjectDetection()){
            detectedObjects = imageObjectDetector.detectObjects(imageRequestDTO);

            // If object detection was requested but failed, return null for 500
            if (detectedObjects == null){
                return null;
            }
        }

        checkImageLabel(imageRequestDTO, detectedObjects);

        // TODO: Create hash to reduce duplicates?
        //https://www.hackerfactor.com/blog/index.php?/archives/432-Looks-Like-It.html

        int imageId = imageDAO.addImage(imageRequestDTO, localFilePath, detectedObjects);

        String objects = detectedObjects == null ? "" :
                String.join(",", Arrays.stream(detectedObjects).map(DetectedObjectsDTO::getName).toArray(String[]::new));

        return new ImageDataDTO(imageRequestDTO, getImageData(localFilePath), localFilePath, imageId, objects);
    }

    public ResponseEntity getImageMetadata(int imageId){
        ImageDataDTO imageDataDTO = imageDAO.getImageMetadata(imageId);

        try {
            imageDataDTO.setImageData(FileUtils.readFileToByteArray(new File(imageDataDTO.getUrl())));
        } catch (Exception e){
            log.error("Unable to get image data from file {}", imageDataDTO.getUrl());
        }

        if (imageDataDTO != null){
            return ResponseEntity.ok(new ImageResponseDTO(imageDataDTO));
        } else {
            return ResponseEntity.internalServerError().build();
        }
    }

    public ResponseEntity getAllImageMetadata(){
        List<ImageDataDTO> allImageData = imageDAO.getAllImageData();

        if (allImageData != null){
            return ResponseEntity.ok(new ImagesResponseDTO(allImageData));
        } else {
            return ResponseEntity.internalServerError().build();
        }
    }

    public ResponseEntity getMatchingImages(String[] objects){
        List<ImageDataDTO> matchingImages = imageDAO.getMatchingImages(objects);

        if (matchingImages != null){
            for (ImageDataDTO imageDataDTO : matchingImages) {
                try {
                    imageDataDTO.setImageData(FileUtils.readFileToByteArray(new File(imageDataDTO.getUrl())));
                } catch (Exception e) {
                    log.error("Unable to get image data from file {}", imageDataDTO.getUrl());
                }
            }

            return ResponseEntity.ok(new ImagesResponseDTO(matchingImages));
        } else {
            return ResponseEntity.internalServerError().build();
        }
    }

    public ResponseEntity getImage(String url){
        try {
            if (url != null){
                String base64decoded = new String(Base64.getDecoder().decode(url));
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(FileUtils.readFileToByteArray(new File(base64decoded)));
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e){
            log.error("Unable to provide image for url {}", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    public String downloadFile(ImageRequestDTO imageRequestDTO) throws IOException {
        File tempFile;
        if (imageRequestDTO.getImageUrl() != null) {
            String ext = FilenameUtils.getExtension(imageRequestDTO.getImageUrl());

            tempFile = File.createTempFile("image", "." + ext);

            FileUtils.copyURLToFile(new URL(imageRequestDTO.getImageUrl()), tempFile, 10000, 10000);
        } else {
            // TODO download file from byte array?
            tempFile = File.createTempFile("image", ".jpg");

            FileUtils.writeByteArrayToFile(tempFile, imageRequestDTO.getImageData());
        }

        return tempFile.getAbsolutePath();
    }
    void checkImageLabel(ImageRequestDTO imageRequestDTO, DetectedObjectsDTO[] detectedObjects){
        // TODO: Use categorizers or other endpoint for more generic/descriptive labels?
        if (imageRequestDTO.getImageLabel() == null) {
            if (detectedObjects != null) {
                imageRequestDTO.setImageLabel(createLabelWithTags(detectedObjects));
            } else {
                // TODO - use incoming filename?
                imageRequestDTO.setImageLabel("no-label-" + UUID.randomUUID());
            }
        }
    }

    /**
     * Create a descriptive label by using tags in descending order of confidence (as they're returned by imagga). DB
     * limit is 128 chars.
     * @param detectedObjects
     * @return
     */
    String createLabelWithTags(DetectedObjectsDTO[] detectedObjects){
        String objects = Arrays.stream(detectedObjects).map(obj -> obj.getName()).collect(Collectors.joining(","));
        if (objects.length() > 127){
            return objects.substring(0, 125) + "...";
        } else {
            return objects;
        }
    }

    byte[] getImageData(String path){
        try {
            return FileUtils.readFileToByteArray(new File(path));
        } catch (Exception e){
            log.error("Unable to read bytes from path {}", path, e);
            return null;
        }
    }
}
