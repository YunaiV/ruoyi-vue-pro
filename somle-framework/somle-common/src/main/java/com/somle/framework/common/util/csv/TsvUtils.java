package com.somle.framework.common.util.csv;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author: c-tao
 * @date: 2024/12/19 9:12
 */
public class TsvUtils {

    private static final CsvReaderX reader = new CsvReaderX('\t');

    public static <T> List<T> toBean(String csvString, Class<T> clazz) {
        return reader.toBean(csvString, clazz);
    }

    public static List<Map<String, String>> toMapList(String csvString) {
        return reader.toMapList(csvString);
    }

    public static String modifyHeader(String csv, Function<String, String> headerMapper) {
        return reader.modifyHeader(csv, headerMapper);
    }
}