package cn.iocoder.yudao.framework.common.util.csv;

import cn.hutool.core.text.csv.CsvReader;

import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author: c-tao
 * @date: 2024/12/19 9:12
 */
public class CsvReaderX extends CsvReader {
    private char SEPERATOR;
    private CsvReader reader;

    public CsvReaderX(char seperator) {
        SEPERATOR = seperator;
        reader = new CsvReader();
        reader.setFieldSeparator(seperator);
    }

    public <T> List<T> toBean(String csvString, Class<T> clazz) {
        // Read the CSV file using Hutool's CsvReader

        // Parse the CSV into a list of maps
        List<T> beanList = reader.read(
            new StringReader(csvString), clazz
        );
        return beanList;
    }

    public List<Map<String, String>> toMapList(String csvString) {
        // Read the CSV file using Hutool's CsvReader

        // Parse the CSV into a list of maps
        List<Map<String, String>> mapList = reader.readMapList(
            new StringReader(csvString)
        );
        return mapList;
    }



    public String modifyHeader(String csvString, Function<String, String> headerMapper) {
        // Find the first newline character to separate header and data
        int firstNewlineIndex = csvString.indexOf('\n');
        if (firstNewlineIndex == -1) {
            // Handle the case where there is no newline (only header exists)
            String[] headers = csvString.split(Character.toString(SEPERATOR));
            for (int i = 0; i < headers.length; i++) {
                headers[i] = headerMapper.apply(headers[i]);
            }
            return String.join(Character.toString(SEPERATOR), headers);
        }

        // Extract header and data parts
        String header = csvString.substring(0, firstNewlineIndex);
        String data = csvString.substring(firstNewlineIndex + 1);

        // Transform the header
        String[] headers = header.split(Character.toString(SEPERATOR));
        for (int i = 0; i < headers.length; i++) {
            headers[i] = headerMapper.apply(headers[i]);
        }
        String modifiedHeader = String.join(Character.toString(SEPERATOR), headers);

        // Combine the modified header with the rest of the data
        return modifiedHeader + "\n" + data;
    }
}