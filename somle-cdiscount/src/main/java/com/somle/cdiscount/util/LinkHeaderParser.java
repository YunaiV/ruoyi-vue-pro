package com.somle.cdiscount.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkHeaderParser {

    // 正则匹配单个 <url>; rel="type" 条目
    private static final Pattern LINK_PATTERN = Pattern
        .compile("<(?:[^>]*\\?)?(.*?)>;\\s*rel\\s*=\\s*\"(.*?)\"");

    public static Map<String, String> parseLinkHeader(String linkHeader) {
        Map<String, String> result = new HashMap<>();

        if (linkHeader == null || linkHeader.isEmpty()) {
            return result;
        }

        String[] links = linkHeader.split(",");
        for (String link : links) {
            Matcher matcher = LINK_PATTERN.matcher(link.trim());
            if (matcher.find()) {
                String queryParams = matcher.group(1);
                String rel = matcher.group(2);

                // 从查询参数中提取 cursor
                String cursor = extractParam(queryParams, "cursor");
                if (cursor != null) {
//                    StringBuffer sb = new StringBuffer();
//                    for (int i = 0; i < cursor.toCharArray().length - 1; i++) {
//                        sb.append(cursor.toCharArray()[i]);
//                    }
                    result.put(rel, cursor);
                }
            }
        }

        return result;
    }

    private static String extractParam(String queryString, String paramName) {
        if (queryString == null || paramName == null) return null;

        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2 && paramName.equals(kv[0])) {
                return kv[1];
            }
        }
        return null;
    }

    // 测试用例
    public static void main(String[] args) {
        String linkHeader = "</products?limit=12&fields=*>; rel=\"first\",</products?cursor=NjdjMmIwYTUxNzE5Y2ViOGI4MDc0YmQ5fHByZXY=&limit=12&fields=*>; rel=\"prev\",</products?cursor=NjdjN2Y2YzUwNDI2ZTBlMjBiOWNhM2UyfG5leHQ=&limit=12&fields=*>; rel=\"next\"";

        Map<String, String> cursors = parseLinkHeader(linkHeader);
        System.out.println("Cursors by rel: " + cursors);
    }
}