package cn.iocoder.yudao.module.system.framework.sms.core.client.utils;


import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 华为短信待签名request
 *
 * @author scholar
 * @since 2024/6/02 11:55
 */
public class HuaweiRequest {
    private String key = null;
    private String secret = null;
    private String method = null;
    private String url = null;
    private String body = null;
    private String fragment = null;
    private Map<String, String> headers = new Hashtable();
    private Map<String, List<String>> queryString = new Hashtable();
    private static final Pattern PATTERN = Pattern.compile("^(?i)(post|put|patch|delete|get|options|head)$");

    public HuaweiRequest() {
    }

    /** @deprecated */
    @Deprecated
    public String getRegion() {
        return "";
    }

    /** @deprecated */
    @Deprecated
    public String getServiceName() {
        return "";
    }

    public String getKey() {
        return this.key;
    }

    public String getSecrect() {
        return this.secret;
    }

//    public HttpMethodName getMethod() {
//        return HttpMethodName.valueOf(this.method.toUpperCase(Locale.getDefault()));
//    }

    public String getBody() {
        return this.body;
    }

    public Map<String, String> getHeaders() {
        return this.headers;
    }

    /** @deprecated */
    @Deprecated
    public void setRegion(String region) {
    }

    /** @deprecated */
    @Deprecated
    public void setServiceName(String serviceName) {
    }

    public void setAppKey(String appKey) throws RuntimeException {
        if (null != appKey && !appKey.trim().isEmpty()) {
            this.key = appKey;
        } else {
            throw new RuntimeException("appKey can not be empty");
        }
    }

    public void setAppSecrect(String appSecret) throws RuntimeException {
        if (null != appSecret && !appSecret.trim().isEmpty()) {
            this.secret = appSecret;
        } else {
            throw new RuntimeException("appSecrect can not be empty");
        }
    }

    public void setKey(String appKey) throws RuntimeException {
        if (null != appKey && !appKey.trim().isEmpty()) {
            this.key = appKey;
        } else {
            throw new RuntimeException("appKey can not be empty");
        }
    }

    public void setSecret(String appSecret) throws RuntimeException {
        if (null != appSecret && !appSecret.trim().isEmpty()) {
            this.secret = appSecret;
        } else {
            throw new RuntimeException("appSecrect can not be empty");
        }
    }

    public void setMethod(String method) throws RuntimeException {
        if (null == method) {
            throw new RuntimeException("method can not be empty");
        } else {
            Matcher match = PATTERN.matcher(method);
            if (!match.matches()) {
                throw new RuntimeException("unsupported method");
            } else {
                this.method = method;
            }
        }
    }

    public String getUrl() throws UnsupportedEncodingException {
        StringBuilder uri = new StringBuilder();
        uri.append(this.url);
        if (this.queryString.size() > 0) {
            uri.append("?");
            int loop = 0;
            Iterator var3 = this.queryString.entrySet().iterator();

            while(var3.hasNext()) {
                Map.Entry<String, List<String>> entry = (Map.Entry)var3.next();

                for(Iterator var5 = ((List)entry.getValue()).iterator(); var5.hasNext(); ++loop) {
                    String value = (String)var5.next();
                    if (loop > 0) {
                        uri.append("&");
                    }

                    uri.append(URLEncoder.encode((String)entry.getKey(), "UTF-8"));
                    uri.append("=");
                    uri.append(URLEncoder.encode(value, "UTF-8"));
                }
            }
        }

        if (this.fragment != null) {
            uri.append("#");
            uri.append(this.fragment);
        }

        return uri.toString();
    }

    public void setUrl(String urlRet) throws RuntimeException, UnsupportedEncodingException {
        if (urlRet != null && !urlRet.trim().isEmpty()) {
            int i = urlRet.indexOf(35);
            if (i >= 0) {
                urlRet = urlRet.substring(0, i);
            }

            i = urlRet.indexOf(63);
            this.url = urlRet;
            if (i >= 0) {
                String query = urlRet.substring(i + 1, urlRet.length());
                String[] var4 = query.split("&");
                int var5 = var4.length;

                for(int var6 = 0; var6 < var5; ++var6) {
                    String item = var4[var6];
                    String[] spl = item.split("=", 2);
                    String keyRet = spl[0];
                    String value = "";
                    if (spl.length > 1) {
                        value = spl[1];
                    }

                    if (!keyRet.trim().isEmpty()) {
                        keyRet = URLDecoder.decode(keyRet, "UTF-8");
                        value = URLDecoder.decode(value, "UTF-8");
                        this.addQueryStringParam(keyRet, value);
                    }
                }

                urlRet = urlRet.substring(0, i);
                this.url = urlRet;
            }
        } else {
            throw new RuntimeException("url can not be empty");
        }
    }

    public String getPath() {
        String urlRet = this.url;
        int i = urlRet.indexOf("://");
        if (i >= 0) {
            urlRet = urlRet.substring(i + 3);
        }

        i = urlRet.indexOf(47);
        return i >= 0 ? urlRet.substring(i) : "/";
    }

    public String getHost() {
        String urlRet = this.url;
        int i = urlRet.indexOf("://");
        if (i >= 0) {
            urlRet = urlRet.substring(i + 3);
        }

        i = urlRet.indexOf(47);
        if (i >= 0) {
            urlRet = urlRet.substring(0, i);
        }

        return urlRet;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void addQueryStringParam(String name, String value) {
        List<String> paramList = (List)this.queryString.get(name);
        if (paramList == null) {
            paramList = new ArrayList();
            this.queryString.put(name, paramList);
        }

        ((List)paramList).add(value);
    }

    public Map<String, List<String>> getQueryStringParams() {
        return this.queryString;
    }

    public String getFragment() {
        return this.fragment;
    }

    public void setFragment(String fragment) throws RuntimeException, UnsupportedEncodingException {
        if (fragment != null && !fragment.trim().isEmpty()) {
            this.fragment = URLEncoder.encode(fragment, "UTF-8");
        } else {
            throw new RuntimeException("fragment can not be empty");
        }
    }

    public void addHeader(String name, String value) {
        if (name != null && !name.trim().isEmpty()) {
            this.headers.put(name, value);
        }
    }
}
