package cn.iocoder.yudao.module.mes.dal.redis;

/**
 * MES Redis Key 枚举类
 *
 * @author 芋道源码
 */
public interface RedisKeyConstants {

    /**
     * 编码规则的缓存
     *
     * KEY 格式：mes:md:auto_code:{ruleId}:{cycleKey}
     * VALUE 数据格式：流水号自增
     */
    String AUTO_CODE = "mes:md:auto_code:";

}
