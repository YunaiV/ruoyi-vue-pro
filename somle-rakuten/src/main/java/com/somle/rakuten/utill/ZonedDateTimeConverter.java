package com.somle.rakuten.utill;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ZonedDateTimeConverter {

    // 日期格式化模式，包含时区偏移
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    /**
     * 将 ZonedDateTime 转换为 String
     *
     * @param zonedDateTime 要转换的 ZonedDateTime 对象
     * @return 格式化后的日期字符串
     */
    public static String convertToString(ZonedDateTime zonedDateTime) {
        if (zonedDateTime == null) {
            return null;
        }
        return zonedDateTime.format(formatter);
    }

    public static void main(String[] args) {
        // 示例：当前时间（使用日本时间）
        ZonedDateTime now = ZonedDateTime.now();  // 当前时区时间
        String formattedDate = convertToString(now);
        System.out.println("Formatted ZonedDateTime: " + formattedDate);

        // 示例：指定时区（例如：Asia/Tokyo）
        ZonedDateTime tokyoTime = ZonedDateTime.now(java.time.ZoneId.of("Asia/Tokyo"));
        String tokyoFormattedDate = convertToString(tokyoTime);
        System.out.println("Formatted Tokyo ZonedDateTime: " + tokyoFormattedDate);
    }
}
