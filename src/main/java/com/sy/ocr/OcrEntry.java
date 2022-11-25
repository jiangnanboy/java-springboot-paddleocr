package com.sy.ocr;

import java.util.Arrays;

/**
 * @author sy
 * @date 2022/11/23 22:05
 */
public class OcrEntry {
    public String text;
    public int[][] box;
    public double score;

    @Override
    public String toString() {
        return "RecognizedText{" +
                "text='" + text + '\'' +
                ", box=" + Arrays.toString(box) +
                ", score=" + score +
                '}';
    }
}
