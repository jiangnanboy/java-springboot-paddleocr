package com.sy.ocr;

import lombok.Data;
import org.springframework.context.annotation.Configuration;

/**
 * @author sy
 * @date 2022/10/24 23:10
 */
@Data
@Configuration
public class OcrProperties {
    private String ocrExe = "/Paddle_CPP/PaddleOCR_json.exe";
}
