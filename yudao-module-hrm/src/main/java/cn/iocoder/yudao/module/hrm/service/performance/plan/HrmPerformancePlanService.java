package cn.iocoder.yudao.module.hrm.service.performance.plan;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessment.HrmPerformanceAssessmentBatchReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.plan.HrmPerformancePlanPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.plan.HrmPerformancePlanSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.plan.HrmPerformancePlanDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * HRM 绩效计划 Service 接口
 *
 * @author 芋道源码
 */
public interface HrmPerformancePlanService {

    /**
     * 创建绩效计划
     *
     * @param reqVO 绩效计划创建参数
     * @return 绩效计划编号
     */
    Long createPerformancePlan(@Valid HrmPerformancePlanSaveReqVO reqVO);

    /**
     * 更新绩效计划
     *
     * @param reqVO 绩效计划更新参数
     */
    void updatePerformancePlan(@Valid HrmPerformancePlanSaveReqVO reqVO);

    /**
     * 删除绩效计划
     *
     * @param id 绩效计划编号
     */
    void deletePerformancePlan(Long id);

    /**
     * 获得绩效计划
     *
     * @param id 绩效计划编号
     * @return 绩效计划
     */
    HrmPerformancePlanDO getPerformancePlan(Long id);

    /**
     * 获得绩效计划分页
     *
     * @param reqVO 分页查询参数
     * @return 绩效计划分页
     */
    PageResult<HrmPerformancePlanDO> getPerformancePlanPage(HrmPerformancePlanPageReqVO reqVO);

    /**
     * 获得绩效计划列表
     *
     * @param ids 计划编号集合
     * @return 绩效计划列表
     */
    List<HrmPerformancePlanDO> getPerformancePlanList(Collection<Long> ids);

    /**
     * 获得绩效计划 Map
     *
     * @param ids 计划编号集合
     * @return 绩效计划 Map
     */
    default Map<Long, HrmPerformancePlanDO> getPerformancePlanMap(Collection<Long> ids) {
        return convertMap(getPerformancePlanList(ids), HrmPerformancePlanDO::getId);
    }

    /**
     * 添加参评员工
     *
     * @param reqVO 批量操作参数
     */
    void addPerformancePlanEmployees(HrmPerformanceAssessmentBatchReqVO reqVO);

    /**
     * 移除参评员工
     *
     * @param reqVO 批量操作参数
     */
    void removePerformancePlanEmployees(HrmPerformanceAssessmentBatchReqVO reqVO);

    /**
     * 启动绩效计划
     *
     * @param id 绩效计划编号
     */
    void startPerformancePlan(Long id);

    /**
     * 开启绩效评分
     *
     * @param id 绩效计划编号
     */
    void openPerformancePlanScoring(Long id);

    /**
     * 发起绩效面谈
     *
     * @param id 绩效计划编号
     */
    void startPerformancePlanInterview(Long id);

    /**
     * 归档绩效计划
     *
     * @param id 绩效计划编号
     */
    void archivePerformancePlan(Long id);

    /**
     * 终止绩效计划
     *
     * @param userId 后台用户编号
     * @param id 绩效计划编号
     */
    void terminatePerformancePlan(Long userId, Long id);

    /**
     * 获得绩效计划状态统计
     *
     * @param reqVO 查询参数
     * @return 状态数量 Map
     */
    Map<Integer, Long> getPerformancePlanStatusCount(HrmPerformancePlanPageReqVO reqVO);

    /**
     * 获得使用指定考核模板的绩效计划数量
     *
     * @param assessmentTemplateId 考核模板编号
     * @return 绩效计划数量
     */
    long getPerformancePlanCountByAssessmentTemplateId(Long assessmentTemplateId);

    /**
     * 获得使用指定结果模板的绩效计划数量
     *
     * @param resultTemplateId 结果模板编号
     * @return 绩效计划数量
     */
    long getPerformancePlanCountByResultTemplateId(Long resultTemplateId);

    /**
     * 获得指定计薪月份的绩效计划列表
     *
     * @param paidForMonth 计薪月份
     * @return 绩效计划列表
     */
    List<HrmPerformancePlanDO> getPerformancePlanListByPaidForMonth(String paidForMonth);

    /**
     * 校验绩效计划是否存在
     *
     * @param id 绩效计划编号
     * @return 绩效计划
     */
    HrmPerformancePlanDO validatePerformancePlanExists(Long id);

    /**
     * 更新绩效计划阶段和可执行操作
     *
     * @param id 绩效计划编号
     * @param stageType 阶段类型
     * @param operationType 可执行操作
     */
    void updatePerformancePlanStageTypeAndOperationType(Long id, Integer stageType, Integer operationType);

}
