//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.somle.lazada.sdk.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

public class LazopLogger {
    private static final Log log = LogFactory.getLog(LazopLogger.class);
    private static final String LOG_SPLIT = "^_^";
    private static String osName = System.getProperties().getProperty("os.name");
    private static boolean needEnableLogger = true;

    public static void setNeedEnableLogger(boolean needEnableLogger) {
        LazopLogger.needEnableLogger = needEnableLogger;
    }

    public static void write(String appKey, String sdkVersion, String apiName, String url, Map<String, String> params, long latency, String errorMessage) {
        if (needEnableLogger) {
            StringBuilder sb = buildLogApi(appKey, sdkVersion, apiName, url, params, latency, errorMessage);
            log.error(sb.toString());
        }
    }

    private static StringBuilder buildLogApi(String appKey, String sdkVersion, String apiName, String url, Map<String, String> params, long latency, String errorMessage) {
        StringBuilder sb = new StringBuilder();
        sb.append(formatDateTime(new Date()));
        sb.append("^_^");
        sb.append(appKey);
        sb.append("^_^");
        sb.append(sdkVersion);
        sb.append("^_^");
        sb.append(apiName);
        sb.append("^_^");
        sb.append(LazopUtils.getIntranetIp());
        sb.append("^_^");
        sb.append(osName);
        sb.append("^_^");
        sb.append(latency);
        sb.append("^_^");
        sb.append(url);

        try {
            sb.append("^_^");
            sb.append(WebUtils.buildQuery(params, "utf-8"));
        } catch (IOException var10) {
        }

        sb.append("^_^");
        sb.append(errorMessage);
        return sb;
    }

    private static String formatDateTime(Date date) {
        return LazopUtils.formatDateTime(date, "yyyy-MM-dd HH:mm:ss.SSS");
    }
}
