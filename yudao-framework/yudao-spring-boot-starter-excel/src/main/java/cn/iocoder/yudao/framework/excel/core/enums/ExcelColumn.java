package cn.iocoder.yudao.framework.excel.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

// TODO @puhui999：列表有办法通过 field name 么？主要考虑一个点，可能导入模版的顺序可能会变
/**
 * Excel 列名枚举
 * 默认枚举 26 列列名如果有需求更多的列名请自行补充
 *
 * @author HUIHUI
 */
@Getter
@AllArgsConstructor
public enum ExcelColumn {

    A(0), B(1), C(2), D(3), E(4), F(5), G(6), H(7), I(8),
    J(9), K(10), L(11), M(12), N(13), O(14), P(15), Q(16),
    R(17), S(18), T(19), U(20), V(21), W(22), X(23), Y(24),
    Z(25);

    /**
     * 列索引
     */
    private final int colNum;

}
