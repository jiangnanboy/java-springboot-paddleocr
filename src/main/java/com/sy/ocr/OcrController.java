package com.sy.ocr;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static com.utils.toFile.deleteTempFile;
import static com.utils.toFile.multipartFiletoFile;


/**
 * @author sy
 * @date 2022/10/13 22:25
 */
@RestController
@RequestMapping("ocr")
@RequiredArgsConstructor
@Slf4j
public class OcrController {

    private final OcrService ocrService;

    /**
     * 返回文字识别后的图片
     *
     * @param multipartFile
     * @param response
     */
    @PostMapping("image")
    @ApiOperation("文字识别，返回图片结果")
    public void ocrImage(@RequestPart MultipartFile multipartFile, HttpServletResponse response) {
        File tFile = null;
        try {
            tFile = multipartFiletoFile(multipartFile);
            OcrEntry[] ocrEntries = ocrService.ocr(tFile.toURI().getPath().replaceFirst("/", ""));
            Image img = ImageFactory.getInstance().fromInputStream(multipartFile.getInputStream());
            BufferedImage resultImage = ocrService.createResultImage(img, ocrEntries);
            response.setContentType("image/png");
            ServletOutputStream os = response.getOutputStream();
            ImageIO.write(resultImage, "PNG", os);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(Optional.ofNullable(tFile).isPresent()) {
                deleteTempFile(tFile);
            }
        }
    }

    /**
     * @param multipartFile
     * @return
     */
    @PostMapping
    @ApiOperation("文字识别，返回JSON结果")
    public OcrEntry[] ocr(@RequestPart MultipartFile multipartFile) {
        File tFile = null;
        OcrEntry[] ocrEntries = null;
        try {
            tFile = multipartFiletoFile(multipartFile);
            ocrEntries = ocrService.ocr(tFile.toURI().getPath().replaceFirst("/", ""));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(Optional.ofNullable(tFile).isPresent()) {
                deleteTempFile(tFile);
            }
        }
        return ocrEntries;
    }

}

