package org.apache.olingo.schema.processor.test.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * 测试资源工具类
 * 用于从resources目录加载XML文件
 */
public class TestResourceLoader {

    private static final String TEST_XML_BASE_PATH = "/test-xml/";

    /**
     * 从resources/test-xml目录加载XML文件内容
     * 
     * @param fileName XML文件名
     * @return XML文件内容字符串
     * @throws IOException 读取文件失败时抛出
     */
    public static String loadXmlContent(String fileName) throws IOException {
        String resourcePath = TEST_XML_BASE_PATH + fileName;
        try (InputStream inputStream = TestResourceLoader.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            return readStreamToString(inputStream);
        }
    }

    /**
     * 读取InputStream内容为字符串（Java 8兼容版本）
     */
    private static String readStreamToString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(StandardCharsets.UTF_8.name());
    }

    /**
     * 从resources/test-xml目录加载XML文件的InputStream
     * 
     * @param fileName XML文件名
     * @return XML文件的InputStream
     * @throws IOException 读取文件失败时抛出
     */
    public static InputStream loadXmlStream(String fileName) throws IOException {
        String resourcePath = TEST_XML_BASE_PATH + fileName;
        InputStream inputStream = TestResourceLoader.class.getResourceAsStream(resourcePath);
        if (inputStream == null) {
            throw new IOException("Resource not found: " + resourcePath);
        }
        return inputStream;
    }

    /**
     * 检查XML文件是否存在
     * 
     * @param fileName XML文件名
     * @return 文件是否存在
     */
    public static boolean xmlFileExists(String fileName) {
        String resourcePath = TEST_XML_BASE_PATH + fileName;
        return TestResourceLoader.class.getResource(resourcePath) != null;
    }
}
