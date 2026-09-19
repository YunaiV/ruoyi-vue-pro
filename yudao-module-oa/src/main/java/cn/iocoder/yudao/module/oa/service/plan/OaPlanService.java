package cn.iocoder.yudao.module.oa.service.plan;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.oa.controller.admin.plan.vo.OaPlanPageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.plan.vo.OaPlanSaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.plan.OaPlanDO;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;

/**
 * OA 工作计划 Service 接口
 *
 * @author 芋道源码
 */
public interface OaPlanService {

    /**
     * 创建工作计划
     *
     * @param createReqVO 创建信息
     * @return 计划编号
     */
    Long createPlan(OaPlanSaveReqVO createReqVO);

    /**
     * 更新工作计划
     *
     * @param updateReqVO 更新信息
     * @param userId 用户编号
     */
    void updatePlan(OaPlanSaveReqVO updateReqVO, Long userId);

    /**
     * 删除工作计划
     *
     * @param id 计划编号
     * @param userId 用户编号
     */
    void deletePlan(Long id, Long userId);

    /**
     * 获得工作计划
     *
     * @param id 计划编号
     * @param userId 用户编号
     * @return 工作计划
     */
    OaPlanDO getPlan(Long id, Long userId);

    /**
     * 获得工作计划分页
     *
     * @param pageReqVO 分页查询
     * @param userId 用户编号
     * @return 工作计划分页
     */
    PageResult<OaPlanDO> getPlanPage(OaPlanPageReqVO pageReqVO, Long userId);

    /**
     * 点评工作计划
     *
     * @param id 计划编号
     * @param comment 点评内容
     * @param userId 点评人用户编号
     */
    void addPlanComment(Long id, String comment, Long userId);

    /**
     * 获得指定成员在统计周期内的最新工作计划 Map
     *
     * @param userIds 成员用户编号集合
     * @param type 计划类型
     * @param createTime 创建时间范围
     * @return 以成员用户编号为 Key 的最新工作计划 Map
     */
    Map<Long, OaPlanDO> getLatestPlanMap(Collection<Long> userIds, Integer type,
                                         LocalDateTime[] createTime);

}
