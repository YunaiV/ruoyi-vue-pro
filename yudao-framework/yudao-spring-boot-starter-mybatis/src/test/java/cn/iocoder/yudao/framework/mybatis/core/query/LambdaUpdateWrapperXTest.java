package cn.iocoder.yudao.framework.mybatis.core.query;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import lombok.Data;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link LambdaUpdateWrapperX} 的单元测试
 *
 * @author 芋道源码
 */
public class LambdaUpdateWrapperXTest {

    @BeforeAll
    public static void initTableInfo() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), TestDO.class);
    }

    @Test
    public void testSetNullIfAbsent_value() {
        // 调用：字段值为 null 时，追加空值更新
        LambdaUpdateWrapperX<TestDO> query = new LambdaUpdateWrapperX<TestDO>()
                .setNullIfAbsent(TestDO::getName, null);

        // 断言
        assertTrue(query.getSqlSet().startsWith("name="));
        assertEquals(1, query.getParamNameValuePairs().size());
        assertTrue(query.getParamNameValuePairs().containsValue(null));

        // 调用，并断言：非 null 值不追加更新，空字符串、空集合、0 和 false 同样保留
        for (Object value : new Object[]{"name", "", Collections.emptyList(), 0, false}) {
            query = new LambdaUpdateWrapperX<TestDO>().setNullIfAbsent(TestDO::getName, value);
            assertNull(query.getSqlSet());
            assertTrue(query.getParamNameValuePairs().isEmpty());
        }
    }

    @Test
    public void testSetNullIfAbsent_entity() {
        // 准备参数
        TestDO entity = new TestDO();
        entity.setId(7L);
        entity.setName("保留原值");

        // 调用
        LambdaUpdateWrapperX<TestDO> query = new LambdaUpdateWrapperX<TestDO>()
                .eq(TestDO::getId, entity.getId())
                .setNullIfAbsent(entity, TestDO::getName, TestDO::getDescription);

        // 断言：仅清空指定的 null 字段，非 null 字段不追加更新
        assertTrue(query.getSqlSet().startsWith("description="));
        assertFalse(query.getSqlSet().contains("name="));
        // 断言：实体只提供字段值，WHERE 条件仍由显式 eq 指定
        assertTrue(query.getSqlSegment().contains("id ="));
        assertNull(query.getEntity());
        assertEquals(2, query.getParamNameValuePairs().size());
        assertTrue(query.getParamNameValuePairs().containsValue(7L));
        assertTrue(query.getParamNameValuePairs().containsValue(null));
    }

    @Data
    @TableName("test_update_data")
    private static class TestDO {

        private Long id;
        private String name;
        private String description;
    }

}
