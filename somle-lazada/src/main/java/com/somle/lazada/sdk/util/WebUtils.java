//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.somle.lazada.sdk.util;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

public abstract class WebUtils {
    private static final String DEFAULT_CHARSET = "UTF-8";
    private static boolean ignoreSSLCheck = true;
    private static boolean ignoreHostCheck = true;

    private WebUtils() {
    }

    public static void setIgnoreSSLCheck(boolean ignoreSSLCheck) {
        WebUtils.ignoreSSLCheck = ignoreSSLCheck;
    }

    public static void setIgnoreHostCheck(boolean ignoreHostCheck) {
        WebUtils.ignoreHostCheck = ignoreHostCheck;
    }

    public static String doGet(String url, Map<String, String> params, int connectTimeout, int readTimeout) throws IOException {
        return doGet(url, params, (Map) null, connectTimeout, readTimeout, "UTF-8", (Proxy) null);
    }

    public static String doGet(String url, Map<String, String> queryParams, Map<String, String> headerParams, int connectTimeout, int readTimeout, String charset, Proxy proxy) throws IOException {
        HttpURLConnection conn = null;
        String rsp = null;

        try {
            String ctype = "application/x-www-form-urlencoded;charset=" + charset;
            String query = buildQuery(queryParams, charset);
            conn = getConnection(buildGetUrl(url, query), "GET", ctype, headerParams, proxy);
            conn.setConnectTimeout(connectTimeout);
            conn.setReadTimeout(readTimeout);
            rsp = getResponseAsString(conn);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }

        }

        return rsp;
    }

    public static String doPost(String url, Map<String, String> params, int connectTimeout, int readTimeout) throws IOException {
        return doPost(url, params, (Map) null, "UTF-8", connectTimeout, readTimeout, (Proxy) null);
    }

    public static String doPost(String url, String body, Map<String, String> headers, String charset, int connectTimeout, int readTimeout) throws IOException {
        String ctype = "text/plain;charset=" + charset;
        byte[] content = body.getBytes(charset);
        return _doPost(url, ctype, content, headers, connectTimeout, readTimeout, (Proxy) null);
    }

    public static String doPost(String url, Map<String, String> queryParams, Map<String, String> headerParams, String charset, int connectTimeout, int readTimeout, Proxy proxy) throws IOException {
        String ctype = "application/x-www-form-urlencoded;charset=" + charset;
        String query = buildQuery(queryParams, charset);
        byte[] content = new byte[0];
        if (query != null) {
            content = query.getBytes(charset);
        }

        return _doPost(url, ctype, content, headerParams, connectTimeout, readTimeout, proxy);
    }

    private static String _doPost(String url, String ctype, byte[] content, Map<String, String> headers, int connectTimeout, int readTimeout, Proxy proxy) throws IOException {
        HttpURLConnection conn = null;
        OutputStream out = null;
        String rsp = null;

        try {
            conn = getConnection(new URL(url), "POST", ctype, headers, proxy);
            conn.setConnectTimeout(connectTimeout);
            conn.setReadTimeout(readTimeout);
            out = conn.getOutputStream();
            out.write(content);
            rsp = getResponseAsString(conn);
        } finally {
            if (out != null) {
                out.close();
            }

            if (conn != null) {
                conn.disconnect();
            }

        }

        return rsp;
    }

    public static String doPost(String url, Map<String, String> queryParams, Map<String, FileItem> fileParams, int connectTimeout, int readTimeout) throws IOException {
        return doPost(url, queryParams, fileParams, (Map) null, "UTF-8", connectTimeout, readTimeout);
    }

    public static String doPost(String url, Map<String, String> queryParams, Map<String, FileItem> fileParams, Map<String, String> headerParams, String charset, int connectTimeout, int readTimeout) throws IOException {
        return fileParams != null && !fileParams.isEmpty() ? _doPostWithFile(url, queryParams, fileParams, headerParams, charset, connectTimeout, readTimeout) : doPost(url, queryParams, headerParams, charset, connectTimeout, readTimeout, (Proxy) null);
    }

    private static String _doPostWithFile(String url, Map<String, String> queryParams, Map<String, FileItem> fileParams, Map<String, String> headerParams, String charset, int connectTimeout, int readTimeout) throws IOException {
        String boundary = String.valueOf(System.nanoTime());
        HttpURLConnection conn = null;
        OutputStream out = null;
        String rsp = null;

        try {
            String ctype = "multipart/form-data;charset=" + charset + ";boundary=" + boundary;
            conn = getConnection(new URL(url), "POST", ctype, headerParams, (Proxy) null);
            conn.setConnectTimeout(connectTimeout);
            conn.setReadTimeout(readTimeout);
            out = conn.getOutputStream();
            byte[] entryBoundaryBytes = ("\r\n--" + boundary + "\r\n").getBytes(charset);

            for (Map.Entry<String, String> textEntry : queryParams.entrySet()) {
                byte[] textBytes = getTextEntry((String) textEntry.getKey(), (String) textEntry.getValue(), charset);
                out.write(entryBoundaryBytes);
                out.write(textBytes);
            }

            for (Map.Entry<String, FileItem> fileEntry : fileParams.entrySet()) {
                FileItem fileItem = (FileItem) fileEntry.getValue();
                if (!fileItem.isValid()) {
                    throw new IOException("FileItem is invalid");
                }

                byte[] fileBytes = getFileEntry((String) fileEntry.getKey(), fileItem.getFileName(), fileItem.getMimeType(), charset);
                out.write(entryBoundaryBytes);
                out.write(fileBytes);
                fileItem.write(out);
            }

            byte[] endBoundaryBytes = ("\r\n--" + boundary + "--\r\n").getBytes(charset);
            out.write(endBoundaryBytes);
            rsp = getResponseAsString(conn);
        } finally {
            if (out != null) {
                out.close();
            }

            if (conn != null) {
                conn.disconnect();
            }

        }

        return rsp;
    }

    private static byte[] getTextEntry(String fieldName, String fieldValue, String charset) throws IOException {
        StringBuilder entry = new StringBuilder();
        entry.append("Content-Disposition:form-data;name=\"");
        entry.append(fieldName);
        entry.append("\"\r\nContent-Type:text/plain\r\n\r\n");
        entry.append(fieldValue);
        return entry.toString().getBytes(charset);
    }

    private static byte[] getFileEntry(String fieldName, String fileName, String mimeType, String charset) throws IOException {
        StringBuilder entry = new StringBuilder();
        entry.append("Content-Disposition:form-data;name=\"");
        entry.append(fieldName);
        entry.append("\";filename=\"");
        entry.append(fileName);
        entry.append("\"\r\nContent-Type:");
        entry.append(mimeType);
        entry.append("\r\n\r\n");
        return entry.toString().getBytes(charset);
    }

    private static HttpURLConnection getConnection(URL url, String method, String ctype, Map<String, String> headers, Proxy proxy) throws IOException {
        HttpURLConnection conn = null;
        if (proxy == null) {
            conn = (HttpURLConnection) url.openConnection();
        } else {
            conn = (HttpURLConnection) url.openConnection(proxy);
        }

        if (conn instanceof HttpsURLConnection) {
            HttpsURLConnection connHttps = (HttpsURLConnection) conn;
            if (ignoreSSLCheck) {
                try {
                    SSLContext ctx = SSLContext.getInstance("TLS");
                    ctx.init((KeyManager[]) null, new TrustManager[]{new TrustAllTrustManager()}, new SecureRandom());
                    connHttps.setSSLSocketFactory(ctx.getSocketFactory());
                    connHttps.setHostnameVerifier(new HostnameVerifier() {
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    });
                } catch (Exception e) {
                    throw new IOException(e.toString());
                }
            } else if (ignoreHostCheck) {
                connHttps.setHostnameVerifier(new HostnameVerifier() {
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });
            }

            conn = connHttps;
        }

        conn.setRequestMethod(method);
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setRequestProperty("Host", url.getHost());
        conn.setRequestProperty("Accept", "text/xml,text/javascript");
        conn.setRequestProperty("User-Agent", "lazop-sdk-java");
        conn.setRequestProperty("Content-Type", ctype);
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                conn.setRequestProperty((String) entry.getKey(), (String) entry.getValue());
            }
        }

        return conn;
    }

    private static URL buildGetUrl(String url, String query) throws IOException {
        return LazopUtils.isEmpty(query) ? new URL(url) : new URL(buildRequestUrl(url, query));
    }

    public static String buildRequestUrl(String url, String... queries) {
        if (queries != null && queries.length != 0) {
            StringBuilder newUrl = new StringBuilder(url);
            boolean hasQuery = url.contains("?");
            boolean hasPrepend = url.endsWith("?") || url.endsWith("&");

            for (String query : queries) {
                if (!LazopUtils.isEmpty(query)) {
                    if (!hasPrepend) {
                        if (hasQuery) {
                            newUrl.append("&");
                        } else {
                            newUrl.append("?");
                            hasQuery = true;
                        }
                    }

                    newUrl.append(query);
                    hasPrepend = false;
                }
            }

            return newUrl.toString();
        } else {
            return url;
        }
    }

    public static String buildRestUrl(String url, String apiName) {
        if (apiName != null && apiName.length() != 0) {
            boolean hasPrepend = url.endsWith("/");
            return hasPrepend ? url + apiName.substring(1) : url + apiName;
        } else {
            return url;
        }
    }

    public static String buildQuery(Map<String, String> params, String charset) throws IOException {
        if (params != null && !params.isEmpty()) {
            StringBuilder query = new StringBuilder();
            Set<Map.Entry<String, String>> entries = params.entrySet();
            boolean hasParam = false;

            for (Map.Entry<String, String> entry : entries) {
                String name = (String) entry.getKey();
                String value = (String) entry.getValue();
                if (LazopUtils.areNotEmpty(new String[]{name, value})) {
                    if (hasParam) {
                        query.append("&");
                    } else {
                        hasParam = true;
                    }

                    query.append(name).append("=").append(URLEncoder.encode(value, charset));
                }
            }

            return query.toString();
        } else {
            return null;
        }
    }

    protected static String getResponseAsString(HttpURLConnection conn) throws IOException {
        String charset = getResponseCharset(conn.getContentType());
        if (conn.getResponseCode() < 400) {
            String contentEncoding = conn.getContentEncoding();
            return "gzip".equalsIgnoreCase(contentEncoding) ? getStreamAsString(new GZIPInputStream(conn.getInputStream()), charset) : getStreamAsString(conn.getInputStream(), charset);
        } else {
            if (conn.getResponseCode() == 400) {
                InputStream error = conn.getErrorStream();
                if (error != null) {
                    return getStreamAsString(error, charset);
                }
            }

            throw new IOException(conn.getResponseCode() + " " + conn.getResponseMessage());
        }
    }

    public static String getStreamAsString(InputStream stream, String charset) throws IOException {
        String var6;
        try {
            Reader reader = new InputStreamReader(stream, charset);
            StringBuilder response = new StringBuilder();
            char[] buff = new char[1024];
            int read = 0;

            while ((read = reader.read(buff)) > 0) {
                response.append(buff, 0, read);
            }

            var6 = response.toString();
        } finally {
            if (stream != null) {
                stream.close();
            }

        }

        return var6;
    }

    public static String getResponseCharset(String ctype) {
        String charset = "UTF-8";
        if (!LazopUtils.isEmpty(ctype)) {
            String[] params = ctype.split(";");

            for (String param : params) {
                param = param.trim();
                if (param.startsWith("charset")) {
                    String[] pair = param.split("=", 2);
                    if (pair.length == 2 && !LazopUtils.isEmpty(pair[1])) {
                        charset = pair[1].trim();
                    }
                    break;
                }
            }
        }

        return charset;
    }

    public static Map<String, String> splitUrlQuery(String query) {
        Map<String, String> result = new HashMap();
        String[] pairs = query.split("&");
        if (pairs != null && pairs.length > 0) {
            for (String pair : pairs) {
                String[] param = pair.split("=", 2);
                if (param != null && param.length == 2) {
                    result.put(param[0], param[1]);
                }
            }
        }

        return result;
    }

    public static class TrustAllTrustManager implements X509TrustManager {
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }
    }
}
