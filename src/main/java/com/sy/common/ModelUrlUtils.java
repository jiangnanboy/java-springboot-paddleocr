package com.sy.common;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.net.URI;

/**
 * @author sy
 * @date 2022/9/15 22:42
 */
@UtilityClass
@Slf4j
public class ModelUrlUtils {

    /**
     * 获取模型url，如果是http或file开头，直接返回
     * @param name 模型名称
     * @return url
     */
    @SneakyThrows
    public static String getRealUrl(String name) {
        if (name.startsWith("http") || name.startsWith("file:")) {
            System.out.println("model url is : " + name);
            return name;
        }

        URI uri = null;
        try {
            uri = new ClassPathResource(name).getURI();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("model uri of " + name + " is " + uri);
        if (uri.toString().startsWith("jar:")) {
            return "jar://" + name;
        }

        return uri.toString();
    }

}
