package cn.iocoder.yudao.framework.mybatis.core.query;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import lombok.Data;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link LambdaQueryWrapperX} 的单元测试
 *
 * @author 芋道源码
 */
public class LambdaQueryWrapperXTest {

    @BeforeAll
    public static void initTableInfo() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), TestDO.class);
    }

    @Test
    public void testBetweenIfPresent_overlap() {
        // 调用，并断言：空范围不追加条件
        assertEquals("", new LambdaQueryWrapperX<TestDO>()
                .betweenIfPresent(TestDO::getStartTime, TestDO::getEndTime, null)
                .getSqlSegment());

        // 调用，并断言：双边范围使用闭区间重叠条件
        LocalDateTime beginTime = LocalDateTime.of(2026, 8, 1, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 8, 31, 23, 59);
        String sqlSegment = new LambdaQueryWrapperX<TestDO>()
                .betweenIfPresent(TestDO::getStartTime, TestDO::getEndTime,
                        new LocalDateTime[]{beginTime, endTime})
                .getSqlSegment();
        assertTrue(sqlSegment.contains("start_time <="));
        assertTrue(sqlSegment.contains("end_time >="));

        // 调用，并断言：单边范围只追加对应的重叠边界
        sqlSegment = new LambdaQueryWrapperX<TestDO>()
                .betweenIfPresent(TestDO::getStartTime, TestDO::getEndTime,
                        new LocalDateTime[]{beginTime})
                .getSqlSegment();
        assertTrue(sqlSegment.contains("end_time >="));
        sqlSegment = new LambdaQueryWrapperX<TestDO>()
                .betweenIfPresent(TestDO::getStartTime, TestDO::getEndTime,
                        new LocalDateTime[]{null, endTime})
                .getSqlSegment();
        assertTrue(sqlSegment.contains("start_time <="));
    }

    @Data
    @TableName("test_data")
    private static class TestDO {

        private LocalDateTime startTime;
        private LocalDateTime endTime;

    }

}
