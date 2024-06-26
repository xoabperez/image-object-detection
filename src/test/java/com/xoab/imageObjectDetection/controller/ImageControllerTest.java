package com.xoab.imageObjectDetection.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xoab.imageObjectDetection.dto.responseDTOs.ImageDataDTO;
import com.xoab.imageObjectDetection.dto.ImageRequestDTO;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ImageControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void addImageFromUrl() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        ImageRequestDTO imageRequestDTO = new ImageRequestDTO();
        imageRequestDTO.setImageLabel("medieval-barn");
        imageRequestDTO.setImageUrl("https://upload.wikimedia.org/wikipedia/commons/8/8b/Boerendeel_Rijksmuseum_SK-C-540.jpeg");
        imageRequestDTO.setEnableObjectDetection(true);

        MvcResult mvcResult = mockMvc.perform(post("/images")
                    .content(objectMapper.writeValueAsString(imageRequestDTO))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody  = mvcResult.getResponse().getContentAsString();

        ImageDataDTO imageDataDTO = objectMapper.readValue(responseBody, ImageDataDTO.class);

        assertNotNull(imageDataDTO);
        assertEquals(imageRequestDTO.getImageLabel(), imageDataDTO.getLabel());
        assertTrue(imageDataDTO.getObjects().contains("animal"));
    }

    @Test
    void addImageFromBytes() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        ImageRequestDTO imageRequestDTO = new ImageRequestDTO();
        imageRequestDTO.setImageLabel("bunny-dog-cat");
        imageRequestDTO.setImageData(FileUtils.readFileToByteArray(new File(getClass().getResource("/bunny-dog-cat.jpg").getFile())));
        imageRequestDTO.setEnableObjectDetection(true);

        MvcResult mvcResult = mockMvc.perform(post("/images")
                        .content(objectMapper.writeValueAsString(imageRequestDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody  = mvcResult.getResponse().getContentAsString();

        ImageDataDTO imageDataDTO = objectMapper.readValue(responseBody, ImageDataDTO.class);

        assertNotNull(imageDataDTO);
        assertEquals(imageRequestDTO.getImageLabel(), imageDataDTO.getLabel());
        assertTrue(imageDataDTO.getObjects().contains("animal"));
    }

    @Test
    void addImageAndExpectValidationError() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        ImageRequestDTO imageRequestDTO = new ImageRequestDTO();
        imageRequestDTO.setImageLabel("medieval-barn");
        imageRequestDTO.setImageUrl("https://upload.wikimedia.org/wikipedia/commons/8/8b/Boerendeel_Rijksmuseum_SK-C-540.jpeg");
        imageRequestDTO.setImageData(new byte[]{10});
        imageRequestDTO.setEnableObjectDetection(true);

        mockMvc.perform(post("/images")
                        .content(objectMapper.writeValueAsString(imageRequestDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}