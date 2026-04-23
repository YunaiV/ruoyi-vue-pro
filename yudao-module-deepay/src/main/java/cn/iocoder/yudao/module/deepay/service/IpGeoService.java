package cn.iocoder.yudao.module.deepay.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * IpGeoService — IP 地址 → 国家代码（ISO 3166-1 alpha-2）。
 *
 * <h3>数据源</h3>
 * <p>使用 <a href="http://ip-api.com">ip-api.com</a>（免费，无需 Key，每分钟 45 次）。</p>
 *
 * <h3>缓存</h3>
 * <p>使用 Spring {@code @Cacheable("ip_country")}，同一 IP 只查一次（Caffeine 缓存）。</p>
 *
 * <h3>Fallback</h3>
 * <p>接口超时/失败 → 返回 "EU"（映射为 EUR），保证系统不炸。</p>
 *
 * <h3>注意</h3>
 * <ul>
 *   <li>不要信前端传的 currency，后端统一根据 IP 判断</li>
 *   <li>内网/本地 IP（127.x / 10.x / 192.168.x）返回默认 "EU"</li>
 * </ul>
 */
@Service
public class IpGeoService {

    private static final Logger log = LoggerFactory.getLogger(IpGeoService.class);

    private static final String IP_API_URL = "http://ip-api.com/json/";
    private static final String DEFAULT_COUNTRY = "EU";   // → EUR

    private final HttpClient   httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * 根据 IP 地址获取国家代码（带本地缓存，同IP只查一次）。
     *
     * @param ip 客户端 IP（支持 IPv4/IPv6）
     * @return ISO 3166-1 alpha-2 国家代码（US / CN / DE …）；失败返回 "EU"
     */
    @Cacheable(value = "ip_country", key = "#ip", unless = "#result == null")
    public String detectCountry(String ip) {
        if (isPrivateIp(ip)) {
            log.debug("[IpGeoService] 私有IP {} → 默认 {}", ip, DEFAULT_COUNTRY);
            return DEFAULT_COUNTRY;
        }
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(IP_API_URL + ip + "?fields=countryCode,status"))
                    .timeout(Duration.ofSeconds(3))
                    .GET().build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                JsonNode node = mapper.readTree(resp.body());
                String status      = node.path("status").asText();
                String countryCode = node.path("countryCode").asText();
                if ("success".equals(status) && !countryCode.isBlank()) {
                    log.debug("[IpGeoService] {} → {}", ip, countryCode);
                    return countryCode.toUpperCase();
                }
            }
        } catch (Exception e) {
            log.debug("[IpGeoService] IP查询失败 ip={} fallback={}", ip, DEFAULT_COUNTRY);
        }
        return DEFAULT_COUNTRY;
    }

    // ====================================================================
    // helpers
    // ====================================================================

    /**
     * 判断是否为私有/本地 IP（内网不查外部）。
     */
    public static boolean isPrivateIp(String ip) {
        if (ip == null || ip.isBlank()) return true;
        return ip.startsWith("127.")
                || ip.startsWith("10.")
                || ip.startsWith("192.168.")
                || ip.startsWith("172.")      // 172.16~31 严格判断简化
                || ip.equals("0:0:0:0:0:0:0:1")
                || ip.equals("::1")
                || ip.equals("localhost");
    }
}
