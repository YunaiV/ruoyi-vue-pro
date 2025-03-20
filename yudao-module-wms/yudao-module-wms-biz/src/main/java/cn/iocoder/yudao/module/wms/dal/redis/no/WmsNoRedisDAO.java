package cn.iocoder.yudao.module.wms.dal.redis.no;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.StrPool;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;


/**
 * WMS 单据序号的 Redis DAO
 *
 * @author LeeFJ
 */
@Repository
public class WmsNoRedisDAO {

    /**
     * 序号的缓存
     *
     * KEY 格式：trade_no:{prefix}
     * VALUE 数据格式：编号自增
     */
    private static final String NO = "erp:seq_no:";

    /**
     * 入库单单据号前缀
     */
    public static final String INBOUND_NO_PREFIX = "INBD";
    /**
     * 出库单单据号前缀
     */
    public static final String OUTBOUND_NO_PREFIX = "OUBD";



    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 生成序号，使用当前日期，格式为 {PREFIX} + "_" + yyyyMMdd + "_" + 6 位自增
     * 例如说：QTRK-20211009-000001 （没有中间空格）
     *
     * @param prefix 前缀
     * @return 序号
     */
    public String generate(String prefix, ErrorCode message) {
        // 递增序号
        String noPrefix = prefix + StrPool.DASHED + DateUtil.format(LocalDateTime.now(), DatePattern.PURE_DATE_PATTERN);
        String key = NO + noPrefix;
        Long no = stringRedisTemplate.opsForValue().increment(key);
        //判断no是否大于6位数
        ThrowUtil.ifGreater(no, 999999L, message);
        // 设置过期时间
        stringRedisTemplate.expire(key, Duration.ofDays(1L));
        return noPrefix + StrPool.DASHED + String.format("%06d", no);
    }

}
