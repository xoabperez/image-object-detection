package com.xoab.imageObjectDetection.helper;

import com.xoab.imageObjectDetection.dto.DetectedObjectsDTO;
import com.xoab.imageObjectDetection.dto.ImageRequestDTO;
import com.xoab.imageObjectDetection.dto.imagga.ImaggaResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;

@Component
@Slf4j
public class ImaggaHelper implements ImageObjectDetector {

    WebClient client = WebClient.create("https://api.imagga.com/v2");

    @Value("${imagga.basic.auth}")
    private String BASIC_AUTH;

    public DetectedObjectsDTO[] detectObjects(ImageRequestDTO imageRequestDTO){

        long startTime = System.currentTimeMillis();

        // TODO: If timeout, need to upload first
        ImaggaResponseDTO response;
        if (imageRequestDTO.getImageUrl() != null){
            response = getImaggaResult(imageRequestDTO.getImageUrl());
        } else {
            response = getImaggaResult(imageRequestDTO.getImageData());
        };

        log.info("Duration of Imagga request was {} ms", System.currentTimeMillis() - startTime);

        if (response != null && response.getStatus().getType().equalsIgnoreCase("success")) {
            return Arrays.stream(response.getResult().getTags())
                    .map(tag -> new DetectedObjectsDTO(tag.getTag().getEn(), tag.getConfidence()))
                    .toArray(DetectedObjectsDTO[]::new);
        } else {
            return null;
        }
    }

    ImaggaResponseDTO getImaggaResult(String url){
        try {
        return client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/tags")
                        .queryParam("image_url", url)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, BASIC_AUTH)
                .retrieve()
                .bodyToMono(ImaggaResponseDTO.class)
                .block();
        } catch (Exception e) {
            log.error("Error trying to send URL {} to Imagga", url, e);
            return null;
        }
    }

    ImaggaResponseDTO getImaggaResult(byte[] data) {
        try {
            // Imagga wants the data in this format
            String crlf = "\r\n";
            String twoHyphens = "--";
            String boundary = "ImageUpload";
            String contentDisposition = "Content-Disposition: form-data; name=\"image\";filename=\"image.jpg\"";

            byte[] startBytes = (twoHyphens + boundary + crlf + contentDisposition + crlf + crlf).getBytes();
            byte[] endBytes = (crlf + twoHyphens + boundary + twoHyphens + crlf).getBytes();
            data = ArrayUtils.addAll(ArrayUtils.addAll(startBytes, data), endBytes);

            return client.post()
                    .uri("/tags")
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, BASIC_AUTH)
                    .header(HttpHeaders.CONNECTION, "Keep-Alive")
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                    .header(HttpHeaders.CONTENT_TYPE, "multipart/form-data;boundary=" + boundary)
                    .body(BodyInserters.fromValue(data))
                    .retrieve()
                    .bodyToMono(ImaggaResponseDTO.class)
                    .block();
        } catch (Exception e) {
            log.error("Error trying to post image to Imagga", e);
            return null;
        }
    }
}
