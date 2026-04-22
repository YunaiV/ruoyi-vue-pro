package cn.iocoder.yudao.module.deepay.scheduler;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayFxRateDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayFxRateMapper;
import cn.iocoder.yudao.module.deepay.service.FxRateService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Map;

/**
 * FxRateScheduler — 汇率定时同步任务。
 *
 * <h3>数据源</h3>
 * <p>使用 <a href="https://api.exchangerate.host">exchangerate.host</a>（免费，无需 Key）。</p>
 *
 * <h3>执行周期</h3>
 * <ul>
 *   <li>每小时整点执行一次（{@code cron = "0 0 * * * ?"}）</li>
 *   <li>启动后立即执行一次，确保系统启动时有最新汇率</li>
 * </ul>
 *
 * <h3>Fallback 策略</h3>
 * <ul>
 *   <li>主接口失败 → 尝试备用接口 fawazahmed0</li>
 *   <li>两个都失败 → 保留上次 DB 数据，打 WARN 日志，系统继续用缓存值正常运行</li>
 * </ul>
 *
 * <h3>支持货币（可按需扩展）</h3>
 * <pre>USD CNY GBP AED JPY KRW SGD</pre>
 */
@Component
public class FxRateScheduler {

    private static final Logger log = LoggerFactory.getLogger(FxRateScheduler.class);

    /** exchangerate.host 免费 API（EUR 基准）*/
    private static final String API_URL =
            "https://api.exchangerate.host/latest?base=EUR&symbols=USD,CNY,GBP,AED,JPY,KRW,SGD";

    /** 备用 API（fawazahmed0，纯静态，极稳定）*/
    private static final String FALLBACK_API_URL =
            "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/eur.json";

    private static final String[] SUPPORTED = {"USD", "CNY", "GBP", "AED", "JPY", "KRW", "SGD"};

    @Resource private DeepayFxRateMapper fxRateMapper;
    @Resource private FxRateService      fxRateService;

    private final HttpClient   httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();

    // ====================================================================
    // 定时任务：每小时更新
    // ====================================================================

    @Scheduled(cron = "0 0 * * * ?")
    public void syncRates() {
        log.info("[FxRateScheduler] 开始同步汇率 source=exchangerate.host");
        if (!doSync()) {
            log.warn("[FxRateScheduler] 主接口失败，尝试备用 source=fawazahmed0");
            doSyncFallback();
        }
    }

    /** 启动时立即同步一次（@PostConstruct 顺序不可靠，用 fixedDelay 延迟 10s）*/
    @Scheduled(initialDelay = 10_000, fixedDelay = Long.MAX_VALUE)
    public void syncOnStartup() {
        log.info("[FxRateScheduler] 启动同步汇率");
        syncRates();
    }

    // ====================================================================
    // 主 API：exchangerate.host
    // ====================================================================

    /**
     * 调 exchangerate.host，解析并写库。
     *
     * @return true=成功；false=失败（触发 fallback）
     */
    boolean doSync() {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .timeout(Duration.ofSeconds(8))
                    .GET().build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() != 200) {
                log.warn("[FxRateScheduler] HTTP {} from exchangerate.host", resp.statusCode());
                return false;
            }

            JsonNode root  = mapper.readTree(resp.body());
            JsonNode rates = root.path("rates");
            if (rates.isMissingNode() || rates.isEmpty()) {
                log.warn("[FxRateScheduler] 返回无 rates 字段 body={}", resp.body().substring(0, Math.min(200, resp.body().length())));
                return false;
            }

            int count = 0;
            Iterator<Map.Entry<String, JsonNode>> fields = rates.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> e = fields.next();
                saveRate(e.getKey(), e.getValue().decimalValue());
                count++;
            }
            log.info("[FxRateScheduler] ✅ 汇率同步完成 count={}", count);
            return count > 0;

        } catch (Exception e) {
            log.error("[FxRateScheduler] 主接口异常", e);
            return false;
        }
    }

    // ====================================================================
    // 备用 API：fawazahmed0（静态 CDN，极稳定）
    // ====================================================================

    void doSyncFallback() {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(FALLBACK_API_URL))
                    .timeout(Duration.ofSeconds(8))
                    .GET().build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() != 200) {
                log.warn("[FxRateScheduler] 备用API HTTP {}", resp.statusCode());
                return;
            }

            // 格式：{ "date":"...", "eur": { "usd":1.08, "cny":7.80, ... } }
            JsonNode root = mapper.readTree(resp.body());
            JsonNode eur  = root.path("eur");
            if (eur.isMissingNode()) {
                log.warn("[FxRateScheduler] 备用API无 eur 字段");
                return;
            }

            int count = 0;
            for (String sym : SUPPORTED) {
                JsonNode val = eur.path(sym.toLowerCase());
                if (!val.isMissingNode()) {
                    saveRate(sym, val.decimalValue());
                    count++;
                }
            }
            log.info("[FxRateScheduler] ✅ 备用汇率同步完成 count={}", count);

        } catch (Exception e) {
            log.error("[FxRateScheduler] 备用接口异常，保留上次DB数据继续运行", e);
        }
    }

    // ====================================================================
    // helpers
    // ====================================================================

    private void saveRate(String currency, BigDecimal rate) {
        if (currency == null || rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) return;
        try {
            DeepayFxRateDO fx = new DeepayFxRateDO();
            fx.setCurrency(currency.toUpperCase());
            fx.setRate(rate.setScale(6, java.math.RoundingMode.HALF_UP));
            fx.setUpdatedAt(LocalDateTime.now());
            fxRateMapper.saveOrUpdate(fx);
            fxRateService.refreshCache(currency.toUpperCase(), fx.getRate());
        } catch (Exception e) {
            log.warn("[FxRateScheduler] 写库失败 currency={}", currency, e);
        }
    }
}
