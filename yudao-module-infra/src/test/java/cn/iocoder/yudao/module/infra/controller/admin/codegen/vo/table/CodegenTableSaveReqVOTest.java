package cn.iocoder.yudao.module.infra.controller.admin.codegen.vo.table;

import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link CodegenTableSaveReqVO} 的单元测试
 *
 * 代码生成时，moduleName / businessName 会被直接拼接进 Java 包声明（见 codegen/java/**\/*.vm 模板首行），
 * className 会作为 Java 类名使用，因此三者都必须符合 Java 标识符规范。
 */
public class CodegenTableSaveReqVOTest {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    private static boolean valid(String property, String value) {
        CodegenTableSaveReqVO vo = new CodegenTableSaveReqVO();
        switch (property) {
            case "moduleName":
                vo.setModuleName(value);
                break;
            case "businessName":
                vo.setBusinessName(value);
                break;
            case "className":
                vo.setClassName(value);
                break;
            default:
                throw new IllegalArgumentException("unknown property: " + property);
        }
        Set<ConstraintViolation<CodegenTableSaveReqVO>> violations = VALIDATOR.validateProperty(vo, property);
        return violations.isEmpty();
    }

    @Test
    public void testModuleName() {
        // 1. 合法：纯小写字母 / 小写字母加数字，且不以数字开头
        assertTrue(valid("moduleName", "system"));
        assertTrue(valid("moduleName", "infra"));
        assertTrue(valid("moduleName", "pms1"));
        // 2. 非法：含连接符 / 下划线 / 空格 / 大写字母 / 数字开头 / 空字符串
        assertFalse(valid("moduleName", "point-goods"));
        assertFalse(valid("moduleName", "point_goods"));
        assertFalse(valid("moduleName", "point goods"));
        assertFalse(valid("moduleName", "PointGoods"));
        assertFalse(valid("moduleName", "1point"));
        assertFalse(valid("moduleName", ""));
    }

    @Test
    public void testBusinessName() {
        // 1. 合法
        assertTrue(valid("businessName", "user"));
        assertTrue(valid("businessName", "pointgoods"));
        assertTrue(valid("businessName", "goods2"));
        // 2. 非法
        assertFalse(valid("businessName", "point-goods"));
        assertFalse(valid("businessName", "point goods"));
        assertFalse(valid("businessName", "PointGoods"));
        assertFalse(valid("businessName", "2goods"));
        assertFalse(valid("businessName", ""));
    }

    @Test
    public void testClassName() {
        // 1. 合法：首字母大写 + 字母数字
        assertTrue(valid("className", "CodegenTable"));
        assertTrue(valid("className", "Dept"));
        assertTrue(valid("className", "User2"));
        // 2. 非法：小写开头 / 含连接符 / 含空格 / 数字开头 / 空字符串
        assertFalse(valid("className", "codegenTable"));
        assertFalse(valid("className", "point-goods"));
        assertFalse(valid("className", "point goods"));
        assertFalse(valid("className", "1Goods"));
        assertFalse(valid("className", ""));
    }

    /**
     * 加严校验不应误伤存量数据：CodegenBuilder#initTableDefault 依据表名自动推导出的取值，
     * 必须全部通过校验（推导逻辑见 CodegenBuilder.java 第 116~120 行）。
     */
    @Test
    public void testAutoDerivedValues() {
        // system_user => system / user / User
        assertTrue(valid("moduleName", "system"));
        assertTrue(valid("businessName", "user"));
        assertTrue(valid("className", "User"));
        // lottery_point_goods => lottery / pointgoods / PointGoods
        assertTrue(valid("moduleName", "lottery"));
        assertTrue(valid("businessName", "pointgoods"));
        assertTrue(valid("className", "PointGoods"));
        // system_dept => system / dept / Dept
        assertTrue(valid("moduleName", "system"));
        assertTrue(valid("businessName", "dept"));
        assertTrue(valid("className", "Dept"));
    }

}
