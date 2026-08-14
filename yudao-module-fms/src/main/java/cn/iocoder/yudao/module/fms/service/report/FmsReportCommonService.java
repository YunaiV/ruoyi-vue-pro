package cn.iocoder.yudao.module.fms.service.report;

import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.subjectbalance.FmsLedgerSubjectBalanceRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportFormulaRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportFormulaUpdateReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportItemRespVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsSubjectDO;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * FMS 报表共用 Service 接口
 *
 * 负责资产负债、利润和现金流量三大报表共用的账簿余额查询、报表公式解析计算和公式覆盖检查
 *
 * @author 芋道源码
 */
public interface FmsReportCommonService {

    /**
     * 获得指定月份区间的科目余额 Map，树形余额扁平化后按科目编号唯一存放
     *
     * @param accountSetId 账套编号
     * @param startMonth 开始月份
     * @param endMonth 结束月份
     * @param userId 用户编号
     * @return 科目编号到余额的 Map
     */
    Map<Long, FmsLedgerSubjectBalanceRespVO> getSubjectBalanceMap(Long accountSetId,
            YearMonth startMonth, YearMonth endMonth, Long userId);

    /**
     * 校验并构造报表公式项，公式项中的科目必须存在且不重复
     *
     * @param updateReqVO 公式更新请求
     * @param minimumRule 允许的最小取数规则
     * @param maximumRule 允许的最大取数规则
     * @param userId 用户编号
     * @return 报表公式项
     */
    List<FmsReportFormulaRespVO> buildReportFormulaList(FmsReportFormulaUpdateReqVO updateReqVO,
            Integer minimumRule, Integer maximumRule, Long userId);

    /**
     * 校验报表项目属于当前账套且允许编辑公式
     *
     * @param accountSetId 账套编号
     * @param itemAccountSetId 报表项目的账套编号
     * @param editable 报表项目是否可编辑
     */
    void validateReportItemEditable(Long accountSetId, Long itemAccountSetId, Boolean editable);

    /**
     * 将模板公式中的科目编码绑定为账套下的科目编号和名称，编码不存在时科目编号置空
     *
     * @param formula 模板公式
     * @param subjectMap 科目编码到科目的 Map
     * @return 绑定后的公式
     */
    String bindSubjectFormula(String formula, Map<String, FmsSubjectDO> subjectMap);

    /**
     * 解析科目公式，空公式返回空列表
     *
     * @param formula 公式
     * @return 公式项列表
     */
    List<FmsReportFormulaRespVO> parseSubjectFormula(String formula);

    /**
     * 解析科目公式中的科目编号集合，空公式和行次公式返回空集合
     *
     * @param formula 公式
     * @return 科目编号集合
     */
    Set<Long> parseFormulaSubjectIds(String formula);

    /**
     * 判断一级科目自身或下级科目是否已纳入报表公式
     *
     * @param rootSubject 一级科目
     * @param subjects 科目列表
     * @param formulaSubjectIds 公式中的科目编号集合
     * @return 是否已纳入报表公式
     */
    boolean isSubjectTreeMapped(FmsSubjectDO rootSubject, List<FmsSubjectDO> subjects,
            Set<Long> formulaSubjectIds);

    /**
     * 判断是否为一级科目
     *
     * @param subject 科目
     * @return 是否一级科目
     */
    boolean isRootSubject(FmsSubjectDO subject);

    /**
     * 判断是否为行次公式，例如 ["L1-L2"]
     *
     * @param formula 公式
     * @return 是否行次公式
     */
    boolean isLineFormula(String formula);

    /**
     * 按运算符累加或扣减金额
     *
     * @param result 累计结果
     * @param amount 金额
     * @param operator 运算符
     * @return 计算结果
     */
    BigDecimal applyOperator(BigDecimal result, BigDecimal amount, String operator);

    /**
     * 将公式项的科目名称和编码刷新为科目当前值
     *
     * @param formula 公式项
     * @param subject 科目
     */
    void normalizeSubjectFormula(FmsReportFormulaRespVO formula, FmsSubjectDO subject);

    /**
     * 计算余额类取数规则的金额，按科目余额方向决定借贷相减的方向
     *
     * @param rule 取数规则
     * @param subject 科目
     * @param balanceMap 科目编号到余额的 Map
     * @param subjectMap 科目编号到科目的 Map
     * @param opening 是否取期初余额，否则取期末余额
     * @return 金额
     */
    BigDecimal calculateBalanceAmount(Integer rule, FmsSubjectDO subject,
            Map<Long, FmsLedgerSubjectBalanceRespVO> balanceMap,
            Map<Long, FmsSubjectDO> subjectMap, boolean opening);

    /**
     * 计算发生额类取数规则的金额，损益发生额按科目余额方向决定借贷相减的方向
     *
     * @param rule 取数规则
     * @param subject 科目
     * @param balance 科目余额
     * @param current 是否取本期发生额，否则取本年累计发生额
     * @return 金额
     */
    BigDecimal calculateOccurrenceAmount(Integer rule, FmsSubjectDO subject,
            FmsLedgerSubjectBalanceRespVO balance, boolean current);

    /**
     * 计算利润表发生额，正向公式项按既有业务语义抵减反方向发生额
     *
     * @param rule 取数规则
     * @param subject 科目
     * @param balance 科目余额
     * @param current 是否取本期发生额，否则取本年累计发生额
     * @param operator 运算符
     * @return 金额
     */
    BigDecimal calculateIncomeOccurrenceAmount(Integer rule, FmsSubjectDO subject,
            FmsLedgerSubjectBalanceRespVO balance, boolean current, String operator);

    /**
     * 计算报表项目的行次公式金额，例如 ["L1-L2"]
     *
     * @param formula 行次公式
     * @param lineMap 行次到报表项目的 Map
     * @param opening 是否取期初金额
     * @param current 非期初时是否取本期金额，否则取本年累计或期末金额
     * @return 金额
     */
    BigDecimal calculateItemLineFormula(String formula, Map<Integer, FmsReportItemRespVO> lineMap,
            boolean opening, boolean current);

    /**
     * 解析行次公式表达式数组
     *
     * @param formula 行次公式
     * @return 表达式列表
     */
    List<String> parseLineFormula(String formula);

    /**
     * 解析会计期间，格式为 yyyyMM
     *
     * @param month 月份，格式为 yyyy-MM
     * @return 会计期间
     */
    Integer parsePeriod(String month);

    /**
     * 获得报表期间类型，开始和结束期间相同为月度，否则为季度
     *
     * @param fromPeriod 开始会计期间
     * @param toPeriod 结束会计期间
     * @return 报表期间类型
     */
    Integer getPeriodType(Integer fromPeriod, Integer toPeriod);

}
