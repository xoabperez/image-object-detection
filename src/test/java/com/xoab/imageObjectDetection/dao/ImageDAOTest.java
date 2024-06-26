package com.xoab.imageObjectDetection.dao;

import com.xoab.imageObjectDetection.dto.DetectedObjectsDTO;
import com.xoab.imageObjectDetection.dto.responseDTOs.ImageDataDTO;
import com.xoab.imageObjectDetection.dto.responseDTOs.ImageDataDTO;
import com.xoab.imageObjectDetection.dto.ImageRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional("transactionManager")
class ImageDAOTest {

    @Autowired
    ImageDAO imageDAO;

    @Test
    void addImage() {
        ImageRequestDTO imageRequestDTO = new ImageRequestDTO();
        imageRequestDTO.setImageLabel("test");
        imageRequestDTO.setEnableObjectDetection(true);

        String fakePath = UUID.randomUUID().toString();
        DetectedObjectsDTO[] detectedObjectsDTOS = new DetectedObjectsDTO[]{
                new DetectedObjectsDTO("animal", 100.0),
                new DetectedObjectsDTO("dog", 90.0)};

        Integer id = imageDAO.addImage(imageRequestDTO, fakePath, detectedObjectsDTOS);
        assertNotNull(id);
    }

    @Test
    void insertImage() {
        // Use something that won't actually already exist in db
        ImageRequestDTO imageRequestDTO = new ImageRequestDTO();
        imageRequestDTO.setImageLabel("Image Label");
        imageRequestDTO.setEnableObjectDetection(true);

        String fakePath = UUID.randomUUID().toString();

        String[] objects = {"object1", "object2", "object3"};

        Integer id1 = imageDAO.insertImage(imageRequestDTO, fakePath, objects);

        assertTrue(id1 > 0);

        // Should fail due to uniqueness constraint
        Integer id2 = imageDAO.insertImage(imageRequestDTO, fakePath, objects);

        assertNull(id2);
    }

    @Test
    void insertObject() {
        // Use something that won't actually already exist in db
        String uuid = UUID.randomUUID().toString();
        Integer id1 = imageDAO.insertObject(uuid);

        assertTrue(id1 > 0);

        Integer id2 = imageDAO.insertObject(uuid);

        assertEquals(id2, id1);
    }


    @Test
    void insertTag() {
        String uuid = UUID.randomUUID().toString();
        Integer objectId = imageDAO.insertObject(uuid);

        assertTrue(objectId > 0);

        ImageRequestDTO imageRequestDTO = new ImageRequestDTO();
        imageRequestDTO.setImageLabel("test");
        imageRequestDTO.setEnableObjectDetection(true);

        // Must be unique
        String fakePath = UUID.randomUUID().toString();

        Integer imageId = imageDAO.addImage(imageRequestDTO, fakePath, null);
        assertNotNull(imageId);

        // Use test values already present in db
        Integer tagId = imageDAO.insertTag(imageId, objectId, 1.0);

        assertTrue(tagId > 0);

        Integer tagId2 = imageDAO.insertTag(imageId, objectId, 1.0);

        // Uniqueness constraint should prevent duplicate tag
        assertNull(tagId2);
    }

    @Test
    void getImageMetadata() {
        ImageRequestDTO imageRequestDTO = new ImageRequestDTO();
        imageRequestDTO.setImageLabel("test");
        imageRequestDTO.setEnableObjectDetection(true);

        String fakePath = UUID.randomUUID().toString();
        DetectedObjectsDTO[] detectedObjectsDTOS = new DetectedObjectsDTO[]{
                new DetectedObjectsDTO("animal", 100.0),
                new DetectedObjectsDTO("dog", 90.0)};

        Integer id = imageDAO.addImage(imageRequestDTO, fakePath, detectedObjectsDTOS);
        assertNotNull(id);

        ImageDataDTO imageDataDTO = imageDAO.getImageMetadata(id);
        assertNotNull(imageDataDTO);
        assertEquals(id, imageDataDTO.getId());
        assertEquals("test", imageDataDTO.getLabel());
        assertNotNull(imageDataDTO.getUrl());
        assertTrue(imageDataDTO.isObjectDetectionEnabled());
        assertEquals("{animal,dog}", imageDataDTO.getObjects());
    }

    @Test
    void getAllImageData() {
        List<ImageDataDTO> imageDataDTOS = imageDAO.getAllImageData();
        assertNotNull(imageDataDTOS);
        assertFalse(imageDataDTOS.isEmpty());
        assertNotNull(imageDataDTOS.get(0));
        assertNotNull(imageDataDTOS.get(0).getUrl());
    }

    @Test
    void getMatchingImages() {
        List<ImageDataDTO> imageDataDTOS = imageDAO.getMatchingImages(new String[]{"animal", "pen"});
        assertNotNull(imageDataDTOS);
        assertFalse(imageDataDTOS.isEmpty());
    }

}