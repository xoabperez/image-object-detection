package com.xoab.imageObjectDetection.service;

import com.xoab.imageObjectDetection.dao.ImageDAO;
import com.xoab.imageObjectDetection.dto.DetectedObjectsDTO;
import com.xoab.imageObjectDetection.dto.responseDTOs.AllImagesDataDTO;
import com.xoab.imageObjectDetection.dto.responseDTOs.MatchingImagesDataDTO;
import com.xoab.imageObjectDetection.dto.responseDTOs.SingleImageDataDTO;
import com.xoab.imageObjectDetection.dto.ImageRequestDTO;
import com.xoab.imageObjectDetection.helper.ImageObjectDetector;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class ImageService {

    @Autowired
    ImageObjectDetector imageObjectDetector;

    @Autowired
    ImageDAO imageDAO;

    public SingleImageDataDTO addImage(ImageRequestDTO imageRequestDTO, String localFilePath){
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

        return new SingleImageDataDTO(imageRequestDTO, getImageData(localFilePath), localFilePath, imageId, objects);
    }

    public ResponseEntity getImageMetadata(int imageId){
        SingleImageDataDTO singleImageDataDTO = imageDAO.getImageMetadata(imageId);

        try {
            singleImageDataDTO.setImageData(FileUtils.readFileToByteArray(new File(singleImageDataDTO.getUrl())));
        } catch (Exception e){
            log.error("Unable to get image data from file {}", singleImageDataDTO.getUrl());
        }

        if (singleImageDataDTO != null){
            return ResponseEntity.ok(singleImageDataDTO);
        } else {
            return ResponseEntity.internalServerError().build();
        }
    }

    public ResponseEntity getAllImageMetadata(){
        List<AllImagesDataDTO> singleImageDataDTOList = imageDAO.getAllImageData();

        if (singleImageDataDTOList != null){
            return ResponseEntity.ok(singleImageDataDTOList);
        } else {
            return ResponseEntity.internalServerError().build();
        }
    }

    public ResponseEntity getMatchingImages(String[] objects){
        List<MatchingImagesDataDTO> matchingImages = imageDAO.getMatchingImages(objects);

        if (matchingImages != null){
            return ResponseEntity.ok(matchingImages);
        } else {
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
        // Assume no single tag will be greater than 128 chars?
        StringBuilder sb = new StringBuilder(detectedObjects[0].getName());
        int tagNum = 1;
        while(sb.length() + detectedObjects[tagNum].getName().length() < 127){
            sb.append("-");
            sb.append(detectedObjects[tagNum].getName());
            tagNum++;
        }

        return sb.toString();
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
