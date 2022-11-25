package com.sy.ocr;

import ai.djl.modality.cv.Image;
import com.sy.common.ImageUtils;
import com.sy.common.ModelUrlUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author sy
 * @date 2022/11/13 22:02
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OcrService {
    private final OcrProperties prop;

    private OcrCPP ocrCPP = null;

    /**
     * 初始化加载ocr模型
     */
    @PostConstruct
    private void init() {
        System.out.println("加载Ocr执行文件...");
        loadOcrExe();
    }

    /**
     * ocr
     * @param imgFile
     * @return
     */
    public OcrEntry[] ocr(String imgFile) {
        try {
            OcrResponse resp = ocrCPP.runOcrOnPath(imgFile);
            if (resp.code == OcrCode.OK) {
                return resp.data;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 结果图片
     * @param image 图片
     * @return image
     */
    public BufferedImage createResultImage(Image image, OcrEntry[] ocrEntries) {
        return ImageUtils.drawDetectionResults((BufferedImage) image.getWrappedImage(), ocrEntries);
    }

    /**
     * 加载模型
     */
    public void loadOcrExe() {
        Map<String, Object> arguments = new HashMap<>();
//        arguments.put("use_angle_cls", true);
        try {
            ocrCPP = new OcrCPP(new File(ModelUrlUtils.getRealUrl(prop.getOcrExe()).replaceFirst("file:/", "")), arguments);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 服务停止关闭所有模型
     */
    @PreDestroy
    public void closeAll() {
        if(Optional.ofNullable(this.ocrCPP).isPresent()) {
            this.ocrCPP.close();
        }
        System.out.println("close all models");
    }

}
