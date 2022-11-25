package com.utils;

import java.util.Arrays;
import java.util.List;

/**
 * @author sy
 * @date 2022/11/11 21:35
 */
public class FileFormatUtil {
    static List<String> supportFileFormats = CollectionUtil.newArrayList(Arrays.asList("jpeg,jpg,png".split(",")));
    public static boolean checkFormats(String fileFullName) {
        String suffix = fileFullName.substring(fileFullName.lastIndexOf(".") + 1).toLowerCase();
        return supportFileFormats.stream().anyMatch(suffix::contains);
    }
}
