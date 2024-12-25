package com.somle.framework.common.util.csv;

import cn.hutool.core.io.resource.StringResource;
import cn.hutool.core.text.csv.*;
import cn.hutool.core.util.CharsetUtil;
import com.somle.framework.common.util.io.IoUtils;
import com.somle.framework.common.util.string.StrUtils;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author: c-tao
 * @date: 2024/12/19 9:12
 */
public class CsvUtils {
    private CsvUtils() {
    }

    private static final char CSV_SEPARATOR = ',';
    private static final char TSV_SEPARATOR = '\t';

    public static <T> List<T> toBean(String csvString, Class<T> clazz) {
        // Read the CSV file using Hutool's CsvReader
        CsvReader reader = CsvUtil.getReader();

        // Parse the CSV into a list of maps
        List<T> beanList = reader.read(
            new StringReader(csvString), clazz
        );
        return beanList;
    }

    public static List<Map<String, String>> toMapList(String csvString) {
        // Read the CSV file using Hutool's CsvReader
        CsvReader reader = CsvUtil.getReader();

        // Parse the CSV into a list of maps
        List<Map<String, String>> mapList = reader.readMapList(
            new StringReader(csvString)
        );
        return mapList;
    }

    public static String modifyCsvHeader(String csv, Function<String, String> headerMapper) {
        return modifyHeader(csv, headerMapper, CSV_SEPARATOR);
    }

    public static String modifyTsvHeader(String csv, Function<String, String> headerMapper) {
        return modifyHeader(csv, headerMapper, TSV_SEPARATOR);
    }

    public static String modifyHeader(String csv, Function<String, String> headerMapper, char separator) {
        // Find the first newline character to separate header and data
        int firstNewlineIndex = csv.indexOf('\n');
        if (firstNewlineIndex == -1) {
            // Handle the case where there is no newline (only header exists)
            String[] headers = csv.split(Character.toString(separator));
            for (int i = 0; i < headers.length; i++) {
                headers[i] = headerMapper.apply(headers[i]);
            }
            return String.join(Character.toString(separator), headers);
        }

        // Extract header and data parts
        String header = csv.substring(0, firstNewlineIndex);
        String data = csv.substring(firstNewlineIndex + 1);

        // Transform the header
        String[] headers = header.split(Character.toString(separator));
        for (int i = 0; i < headers.length; i++) {
            headers[i] = headerMapper.apply(headers[i]);
        }
        String modifiedHeader = String.join(Character.toString(separator), headers);

        // Combine the modified header with the rest of the data
        return modifiedHeader + "\n" + data;
    }
}