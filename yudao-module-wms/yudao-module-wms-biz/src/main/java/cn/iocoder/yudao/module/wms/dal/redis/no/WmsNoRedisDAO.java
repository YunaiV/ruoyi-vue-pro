package cn.iocoder.yudao.module.wms.dal.redis.no;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
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
     * 库位库存移动单的流水号前缀
     **/
    public static final String STOCK_BIN_MOVE_NO_PREFIX = "SBM";

    /**
     * 逻辑库存移动单的流水号前缀
     **/
    public static final String STOCK_LOGIC_MOVE_NO_PREFIX = "LIM";


    /**
     * 入库单单据号前缀
     */
    public static final String INBOUND_NO_PREFIX = "IBO";
    /**
     * 出库单单据号前缀
     */
    public static final String OUTBOUND_NO_PREFIX = "OBO";

    /**
     * 拣货单单据号前缀
     **/
    public static final String PICKUP_NO_PREFIX = "PIO";

    /**
     * 盘点单据号前缀
     **/
    public static final String STOCKCHECK_NO_PREFIX = "SCD";

    /**
     * 盘点单据号前缀
     **/
    public static final String EXCHANGE_NO_PREFIX = "EXD";




    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 生成序号，使用当前日期，格式为 {PREFIX} + "-" + yyyyMMdd + "-" + 6 位自增
     * 例如说：QTRK-20211009-000001 （没有中间空格）
     *
     * @param prefix 前缀
     * @param seqLength 末尾序列长度
     * @return 序号
     */
    public String generate(String prefix,int seqLength) {
        // 递增序号
        String noPrefix = prefix + "-" + DateUtil.format(LocalDateTime.now(), DatePattern.PURE_DATE_PATTERN);
        String key = NO + noPrefix;
        Long no = stringRedisTemplate.opsForValue().increment(key);
        // 设置过期时间
        stringRedisTemplate.expire(key, Duration.ofDays(1L));
        return noPrefix + "-" + String.format("%0"+seqLength+"d", no);
    }

}
