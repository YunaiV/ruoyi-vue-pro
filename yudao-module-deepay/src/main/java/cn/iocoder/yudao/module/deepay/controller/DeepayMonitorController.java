package cn.iocoder.yudao.module.deepay.controller;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayDesignImageDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayDesignImageMapper;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayOrderMapper;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayTaskMapper;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayUserQuotaMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 运营监控仪表盘（STEP 30）。
 *
 * <pre>
 * GET /api/monitor/dashboard  — 今日核心指标
 * GET /api/monitor/top-images — 全量评分 Top 图
 * </pre>
 *
 * <p>四核心指标：
 * <ol>
 *   <li>今日生成次数（task 表统计）</li>
 *   <li>选图率 = 有 click_count > 0 的图片数 / 总曝光图片数</li>
 *   <li>下单率 = 今日订单数 / 今日任务数</li>
 *   <li>支付率 = 今日 PAID 订单数 / 今日总订单数</li>
 * </ol>
 * </p>
 */
@Tag(name = "Deepay - 运营监控")
@RestController
@RequestMapping("/api/monitor")
@Validated
public class DeepayMonitorController {

    private static final Logger log = LoggerFactory.getLogger(DeepayMonitorController.class);

    @Resource private DeepayTaskMapper        taskMapper;
    @Resource private DeepayOrderMapper       orderMapper;
    @Resource private DeepayDesignImageMapper designImageMapper;
    @Resource private DeepayUserQuotaMapper   quotaMapper;

    // ====================================================================
    // GET /api/monitor/dashboard
    // ====================================================================

    @GetMapping("/dashboard")
    @Operation(summary = "运营监控仪表盘（今日核心指标）")
    public CommonResult<Map<String, Object>> dashboard() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd   = todayStart.plusDays(1);

        // ① 今日生成次数（task 表 created_at 在今日范围内）
        long todayGenerateCount = taskMapper.selectCount(
                new LambdaQueryWrapper<cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayTaskDO>()
                        .ge(cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayTaskDO::getCreatedAt, todayStart)
                        .lt(cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayTaskDO::getCreatedAt, todayEnd));

        long todaySuccessCount = taskMapper.selectCount(
                new LambdaQueryWrapper<cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayTaskDO>()
                        .ge(cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayTaskDO::getCreatedAt, todayStart)
                        .lt(cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayTaskDO::getCreatedAt, todayEnd)
                        .eq(cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayTaskDO::getStatus, "success"));

        // ② 选图率 = 有点击的图片数 / 全部设计图数
        long totalImages   = designImageMapper.selectCount(null);
        long clickedImages = designImageMapper.selectCount(
                new LambdaQueryWrapper<DeepayDesignImageDO>()
                        .gt(DeepayDesignImageDO::getClickCount, 0));
        double selectionRate = totalImages > 0
                ? (double) clickedImages / totalImages : 0.0;

        // ③ 今日订单数 / 今日任务数 → 下单率
        long todayOrders = orderMapper.selectCount(
                new LambdaQueryWrapper<cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayOrderDO>()
                        .ge(cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayOrderDO::getCreatedAt, todayStart)
                        .lt(cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayOrderDO::getCreatedAt, todayEnd));
        double orderRate = todayGenerateCount > 0
                ? (double) todayOrders / todayGenerateCount : 0.0;

        // ④ 支付率 = 今日 PAID 订单数 / 今日总订单数
        long todayPaidOrders = orderMapper.selectCount(
                new LambdaQueryWrapper<cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayOrderDO>()
                        .ge(cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayOrderDO::getCreatedAt, todayStart)
                        .lt(cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayOrderDO::getCreatedAt, todayEnd)
                        .eq(cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayOrderDO::getStatus, "PAID"));
        double paymentRate = todayOrders > 0
                ? (double) todayPaidOrders / todayOrders : 0.0;

        // ⑤ 用户总数（有配额记录）
        long totalUsers = quotaMapper.selectCount(null);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("todayGenerateCount", todayGenerateCount);
        resp.put("todaySuccessCount",  todaySuccessCount);
        resp.put("totalImages",        totalImages);
        resp.put("selectionRate",      pct(selectionRate));
        resp.put("todayOrders",        todayOrders);
        resp.put("orderRate",          pct(orderRate));
        resp.put("todayPaidOrders",    todayPaidOrders);
        resp.put("paymentRate",        pct(paymentRate));
        resp.put("totalUsers",         totalUsers);
        resp.put("date",               LocalDate.now().toString());

        log.info("[Monitor] dashboard 查询完成 date={} generate={} orders={} paid={}",
                LocalDate.now(), todayGenerateCount, todayOrders, todayPaidOrders);
        return success(resp);
    }

    // ====================================================================
    // GET /api/monitor/top-images
    // ====================================================================

    @GetMapping("/top-images")
    @Operation(summary = "综合评分 Top 图片列表")
    public CommonResult<Map<String, Object>> topImages() {
        List<DeepayDesignImageDO> top = designImageMapper.selectTopByCategory(null, 20);
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("images", top);
        resp.put("count",  top.size());
        return success(resp);
    }

    // ====================================================================
    // helpers
    // ====================================================================

    /** 保留两位小数的百分比（0.0 ~ 1.0 → 0.00%~100.00%） */
    private String pct(double ratio) {
        BigDecimal val = BigDecimal.valueOf(ratio * 100)
                .setScale(2, RoundingMode.HALF_UP);
        return val.toPlainString() + "%";
    }

}
