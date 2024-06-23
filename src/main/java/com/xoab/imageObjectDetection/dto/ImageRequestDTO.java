package com.xoab.imageObjectDetection.dto;

import com.xoab.imageObjectDetection.util.OnlyOneNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@OnlyOneNull(
        fields = {"imageData","imageUrl"},
        message= "Either imageData or imageUrl should be provided, not both."
)
public class ImageRequestDTO {

    private byte[] imageData;

    private String imageUrl;

    @Size(max = 128)
    private String imageLabel;

    private boolean enableObjectDetection;
}
