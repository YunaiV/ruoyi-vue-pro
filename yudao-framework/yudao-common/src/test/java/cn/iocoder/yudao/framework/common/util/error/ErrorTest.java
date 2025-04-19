package cn.iocoder.yudao.framework.common.util.error;

import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 错误码与异常抛出的单元测试
 */
public class ErrorTest {

    ErrorCode EXTERNAL_STORAGE_NAME_DUPLICATE = new ErrorCode(2_001_000_000, "审核错误，当前入库单状态为{}，在{}状态时才允许{}");

    @Test
    public void testErrorFormat() {

        try {

            try {
                throw new IllegalArgumentException("参数错误");
            } catch (Exception e) {
                throw exception(EXTERNAL_STORAGE_NAME_DUPLICATE, "A", "B", "C");
            }
        } catch (Exception e) {

            Assert.equals("审核错误，当前入库单状态为A，在B状态时才允许C",e.getMessage());

        }


    }

}
