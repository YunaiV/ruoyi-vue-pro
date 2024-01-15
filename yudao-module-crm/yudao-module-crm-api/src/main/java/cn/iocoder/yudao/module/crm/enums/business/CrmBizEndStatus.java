package cn.iocoder.yudao.module.crm.enums.business;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

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
