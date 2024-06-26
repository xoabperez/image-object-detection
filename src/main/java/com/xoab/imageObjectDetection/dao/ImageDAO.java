package com.xoab.imageObjectDetection.dao;

import com.xoab.imageObjectDetection.dto.DetectedObjectsDTO;
import com.xoab.imageObjectDetection.dto.ImageRequestDTO;
import com.xoab.imageObjectDetection.dto.responseDTOs.ImageDataDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
@Slf4j
public class ImageDAO {

    @Autowired
    JdbcTemplate jdbcTemplate;

    BeanPropertyRowMapper<ImageDataDTO> imageDataDTOBeanPropertyRowMapper = new BeanPropertyRowMapper<>(ImageDataDTO.class);

    public Integer addImage(ImageRequestDTO imageRequestDTO, String localFilePath, DetectedObjectsDTO[] detectedObjects){
        // Insert the image and make sure a valid ID is returned before inserting/retrieving object IDs and then
        // finally inserting tags
        String[] objects = detectedObjects == null ? new String[]{""} :
                Arrays.stream(detectedObjects).map(DetectedObjectsDTO::getName).toArray(String[]::new);

        Integer imageId = insertImage(imageRequestDTO, localFilePath, objects);

        if (detectedObjects != null && imageId != null) {
            for (DetectedObjectsDTO detectedObjectsDTO : detectedObjects) {
                Integer objectId = insertObject(detectedObjectsDTO.getName());

                if (objectId != null) {
                    insertTag(imageId, objectId, detectedObjectsDTO.getConfidence());
                } else {
                    log.warn("Inserting object ID returned no ID, not adding image tag for image {} object {} confidence {}",
                            imageId, detectedObjectsDTO.getName(), detectedObjectsDTO.getConfidence());
                }
            }
        }

        return imageId;
    }

    public Integer insertImage(ImageRequestDTO imageRequestDTO, String localFilePath, String[] objects){
        // File URL should be unique, otherwise an exception may be thrown
        try {
            return jdbcTemplate.queryForObject(
                    "INSERT INTO images (url, label, object_detection_enabled, objects) VALUES (?, ?, ?, ?) RETURNING id",
                    Integer.class,
                    localFilePath,
                    imageRequestDTO.getImageLabel(),
                    imageRequestDTO.isEnableObjectDetection(),
                    objects);
        } catch (Exception e) {
            log.error("Unable to insert image into table", e);
            return null;
        }
    }

    public Integer insertObject(String object){
        // If the object name has already been recorded, the existing ID will be returned, otherwise it will be inserted
        try {
            return jdbcTemplate.queryForObject("SELECT get_object_id(?)", Integer.class, object);
        } catch (Exception e) {
            log.error("Unable to insert object into table", e);
            return null;
        }
    }

    public Integer insertTag(int imageId, int objectId, double confidence){
        // The image-object tag should be unique
        try {
            return jdbcTemplate.queryForObject("INSERT INTO tags (image_id, object_id, confidence) VALUES (?, ?, ?) RETURNING id",
                    Integer.class,
                    imageId,
                    objectId,
                    confidence);
        } catch (Exception e) {
            log.error("Unable to insert tag into table", e);
            return null;
        }
    }

    public ImageDataDTO getImageMetadata(int imageId){
        try {
            List<ImageDataDTO> imageDataDTOS = jdbcTemplate.query(
                    "SELECT id, url, label, object_detection_enabled, objects FROM images WHERE id = ?",
                    imageDataDTOBeanPropertyRowMapper, imageId);

            return imageDataDTOS.get(0);
        } catch (Exception e) {
            log.error("Unable to insert image into table", e);
            return null;
        }
    }

    public List<ImageDataDTO> getMatchingImages(String[] objects){
        try {
            return jdbcTemplate.query(
                    "SELECT images.id, url, label FROM tags JOIN objects ON tags.object_id = objects.id " +
                            "JOIN images ON tags.image_id = images.id WHERE objects.name = ANY(?) ORDER BY confidence DESC",
                    imageDataDTOBeanPropertyRowMapper, (Object) objects);
        } catch (Exception e) {
            log.error("Error finding matching images for objects {}", objects, e);
            return null;
        }
    }

    public List<ImageDataDTO> getAllImageData(){
        try {
            return jdbcTemplate.query(
                    "SELECT id, url, label, object_detection_enabled, objects FROM images",
                    imageDataDTOBeanPropertyRowMapper);
        } catch (Exception e) {
            log.error("Unable to insert image into table", e);
            return null;
        }
    }
}
