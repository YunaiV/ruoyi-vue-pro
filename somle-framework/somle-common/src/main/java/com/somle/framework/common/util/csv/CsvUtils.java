package com.somle.framework.common.util.csv;

import cn.hutool.core.text.csv.*;
import com.somle.framework.common.util.io.IoUtils;
import com.somle.framework.common.util.string.StrUtils;

import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Wqh
 * @date: 2024/12/19 9:12
 */
public class CsvUtils {
    private CsvUtils() {
    }
    private static final char CSV_SEPARATOR = ',';
    private static final char TSV_SEPARATOR = '\t';


    /**
     * 从输入流中读取 TSV 文件并转换为键值对列表
     *
     * @param inputStream 输入流
     * @return 值对列表
     */
    public static List<Map<String, String>> readTsvFromInputStream(InputStream inputStream) {
        return readFromInputStream(inputStream, TSV_SEPARATOR);
    }

    /**
     * 从输入流中读取 CSV 文件并转换为键值对列表
     *
     * @param inputStream 输入流
     * @return 值对列表
     */
    public static List<Map<String, String>> readCsvFromInputStream(InputStream inputStream) {
        return readFromInputStream(inputStream, CSV_SEPARATOR);
    }

    /**
     * 从输入流中读取指定分隔符的 CSV 或 TSV 文件并转换为键值对列表
     *
     * @param inputStream 输入流
     * @param fieldSeparator 字段分隔符
     * @return 驼峰命名键值对列表
     */
    private static List<Map<String, String>> readFromInputStream(InputStream inputStream, char fieldSeparator) {
        Reader reader = IoUtils.createUtf8Reader(inputStream);
            // 创建 CSVReaderConfig 实例，并设置分隔符
            CsvReadConfig config = CsvReadConfig.defaultConfig();
            config.setFieldSeparator(fieldSeparator);
            // 读取 CSV 或 TSV 文件并转换为 Map 列表
            return CsvUtil.getReader(config).readMapList(reader);

    }


}
