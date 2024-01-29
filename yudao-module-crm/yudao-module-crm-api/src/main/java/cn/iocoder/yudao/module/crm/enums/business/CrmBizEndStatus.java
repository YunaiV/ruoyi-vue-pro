package cn.iocoder.yudao.module.crm.enums.business;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

// TODO @lzxhqs：1）title、description、create 可以删除，非标准的 javadoc 注释哈，然后可以在类上加下这个类的注释；2）CrmBizEndStatus 改成 CrmBusinessEndStatus，非必要不缩写哈，可阅读比较重要
/**
 * @author lzxhqs
 * @version 1.0
 * @title CrmBizEndStatus
 * @description
 * @create 2024/1/12
 */
@RequiredArgsConstructor
@Getter
public enum CrmBizEndStatus implements IntArrayValuable {

    WIN(1, "赢单"),
    LOSE(2, "输单"),
    INVALID(3, "无效");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(CrmBizEndStatus::getStatus).toArray();

    // TODO @lzxhqs：这里的方法，建议放到 49 行之后；一般类里是，静态变量，普通变量；静态方法；普通方法
    public static boolean isWin(Integer status) {
        return ObjectUtil.equal(WIN.getStatus(), status);
    }

    public static boolean isLose(Integer status) {
        return ObjectUtil.equal(LOSE.getStatus(), status);
    }

    public static boolean isInvalid(Integer status) {
        return ObjectUtil.equal(INVALID.getStatus(), status);
    }

    /**
     * 场景类型
     */
    private final Integer status;
    /**
     * 场景名称
     */
    private final String name;

    @Override
    public int[] array() {
        return ARRAYS;
    }

}
