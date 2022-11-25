package com.sy.common;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.Rectangle;
import ai.djl.modality.cv.util.NDImageUtils;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;

import com.sy.ocr.OcrEntry;
import lombok.experimental.UtilityClass;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static java.awt.Image.SCALE_DEFAULT;

/**
 * @author sy
 * @date 2022/9/13 21:24
 */
@UtilityClass
public class ImageUtils {

    private static final Map<Integer, Color> colorMap = Map.of(
            0, new Color(200, 0, 0),
            1, new Color(0, 200, 0),
            2, new Color(0, 0, 200),
            3, new Color(200, 200, 0),
            4, new Color(200, 0, 200),
            5, new Color(0, 200, 200)
    );

    /**
     * 旋转图片90度
     *
     * @param image 图片
     * @return 旋转90度后的图片
     */
    public static Image rotateImage(Image image) {
        try (NDManager manager = NDManager.newBaseManager()) {
            NDArray rotated = NDImageUtils.rotate90(image.toNDArray(manager), 1);
            return ImageFactory.getInstance().fromNDArray(rotated);
        }
    }

    public static Image readImg(String imgPath) {
        Image img = null;
        try {
            img = ImageFactory.getInstance().fromFile(Paths.get(imgPath));
            img.getWrappedImage();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return img;
    }

    /**
     * 图片缩放
     *
     * @param original  原始图
     * @param width     宽
     * @param height    高
     * @return          缩放后的图片
     */
    public static BufferedImage scale(BufferedImage original, int width, int height) {
        if (width == original.getWidth() && height == original.getHeight()) {
            return original;
        }

        java.awt.Image scaledInstance = original.getScaledInstance(width, height, SCALE_DEFAULT);
        BufferedImage scaledImage = new BufferedImage(width, height, original.getType());
        scaledImage.getGraphics().drawImage(scaledInstance, 0, 0, null);
        return scaledImage;
    }

    /**
     * 图片缩放
     *
     * @param original  原始图
     * @param width     宽
     * @param height    高
     * @return          缩放后的图片
     */
    public static Image scale(Image original, int width, int height) {
        if (width == original.getWidth() && height == original.getHeight()) {
            return original;
        }

        try(NDManager manager = NDManager.newBaseManager()) {
            NDArray ndArray = original.toNDArray(manager);
            NDArray resize = NDImageUtils.resize(ndArray, width, height, Image.Interpolation.BILINEAR);
            resize = resize.toType(DataType.FLOAT32, false); // INT8
            resize = resize.div(255);
            return ImageFactory.getInstance().fromNDArray(resize);
        }
    }

    public static Image checkSize(Image image) {
        if((image.getHeight() > 960) && (image.getWidth() > 960)) {
            try (NDManager manager = NDManager.newBaseManager()) {
                NDArray ndArray = image.toNDArray(manager);
                ndArray = NDImageUtils.resize(ndArray, 960, 960);
                image = ImageFactory.getInstance().fromNDArray(ndArray.toType(DataType.UINT8, false));
            }
        }
        return image;
    }

    /**
     * 绘制检测结果
     * djl自带的绘制内容太少，只有名称，因此做了修改
     * 以下是djl自带的绘制方法
     * Image img = ImageFactory.getInstance().fromImage(image);
     * img.drawBoundingBoxes(detections);
     * return (BufferedImage) img.getWrappedImage();
     *
     * @param image           图片
     * @param detections    检测结果
     */
    public static BufferedImage drawDetections(BufferedImage image, DetectedObjects detections) {

        Graphics2D g = (Graphics2D) image.getGraphics();
        int stroke = 2;
        g.setStroke(new BasicStroke(stroke));
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();

        List<DetectedObjects.DetectedObject> list = detections.items();
        for (DetectedObjects.DetectedObject result : list) {
            String className = result.getClassName();
            BoundingBox box = result.getBoundingBox();
            double probability = result.getProbability();
            Color color = colorMap.get(Math.abs(className.hashCode() % 6));
            g.setPaint(color);

            Rectangle rectangle = box.getBounds();
            int x = (int) (rectangle.getX() * imageWidth);
            int y = (int) (rectangle.getY() * imageHeight);
            int width = (int) (rectangle.getWidth() * imageWidth);
            int height = (int) (rectangle.getHeight() * imageHeight);
            g.drawRect(x, y, width, height);

            drawText(g, className, probability, x, y, width);
        }
        g.dispose();

        return image;
    }

    private static void drawText(Graphics2D g, String className, double probability, int x, int y, int width) {
        //设置水印的坐标
        String showText = String.format("%s %.0f%%", className, probability * 100);
        g.fillRect(x, y - 30, width, 30);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 25));//设置字体
        g.drawString(showText, x, y - 10);
    }

    /**
     * 对ocr的结果在图片中绘制
     * @param image
     * @param ocrEntries
     * @return
     */
    public static BufferedImage drawDetectionResults(BufferedImage image, OcrEntry[] ocrEntries) {
        Graphics2D g = (Graphics2D) image.getGraphics();
        int stroke = 2;
        g.setStroke(new BasicStroke(stroke));
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for(OcrEntry ocrEntry:ocrEntries) {
            String className = ocrEntry.text;
            double probability = ocrEntry.score;
            int[][] box = ocrEntry.box;
            Color color = colorMap.get(Math.abs(className.hashCode() % 6));
            g.setPaint(color);
            int x = box[0][0];
            int y = box[0][1];
            int width = box[1][0] - box[0][0];
            int height = box[3][1] - box[0][1];
            g.drawRect(x, y, width, height);
            drawText(g, className, probability, x, y, width);
        }
        g.dispose();
        return image;
    }

    /**
     * 获取文字所在的区块图
     *
     * @param image 原始图片
     * @param box 子区块
     * @return 带有文字的image
     */
    public static Image getSubImage(Image image, BoundingBox box) {
        Rectangle rect = box.getBounds();

        int width = image.getWidth();
        int height = image.getHeight();
        int[] recovered = {
                (int) (rect.getX() * width),
                (int) (rect.getY() * height),
                (int) (rect.getWidth() * width),
                (int) (rect.getHeight() * height)
        };
        return image.getSubImage(recovered[0], recovered[1], recovered[2], recovered[3]);
    }

    public static Image getExtendSubImage(Image img, BoundingBox box) {
        Rectangle rect = box.getBounds();
        double[] extended = extendRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
        int width = img.getWidth();
        int height = img.getHeight();
        int[] recovered = {
                (int) (extended[0] * width),
                (int) (extended[1] * height),
                (int) (extended[2] * width),
                (int) (extended[3] * height)
        };
        return img.getSubImage(recovered[0], recovered[1], recovered[2], recovered[3]);
    }

    /**
     * 扩展矩形框
     *
     * @param box 矩形
     * @return 扩展后的大小
     */
    public static BoundingBox extendBox(BoundingBox box) {
        Rectangle rect = box.getBounds();
        double x = rect.getX();
        double y = rect.getY();
        double width = rect.getWidth();
        double height = rect.getHeight();

        double centerX = x + width / 2;
        double centerY = y + height / 2;
        if (width > height) {
            width += height * 2.0;
            height *= 3.0;
        } else {
            height += width * 2.0;
            width *= 3.0;
        }
        double newX = centerX - width / 2 < 0 ? 0 : centerX - width / 2;
        double newY = centerY - height / 2 < 0 ? 0 : centerY - height / 2;
        double newWidth = newX + width > 1 ? 1 - newX : width;
        double newHeight = newY + height > 1 ? 1 - newY : height;
        return new Rectangle(newX, newY, newWidth, newHeight);
    }

    public static double[] extendRect(double xmin, double ymin, double width, double height) {
        double centerx = xmin + width / 2;
        double centery = ymin + height / 2;
        if (width > height) {
            width += height * 2.0;
            height *= 3.0;
        } else {
            height += width * 2.0;
            width *= 3.0;
        }
        double newX = centerx - width / 2 < 0 ? 0 : centerx - width / 2;
        double newY = centery - height / 2 < 0 ? 0 : centery - height / 2;
        double newWidth = newX + width > 1 ? 1 - newX : width;
        double newHeight = newY + height > 1 ? 1 - newY : height;
        return new double[] {newX, newY, newWidth, newHeight};
    }

}

