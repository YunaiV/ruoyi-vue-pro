package cn.iocoder.yudao.adminserver.modules.activiti.service.oa;


import cn.iocoder.yudao.adminserver.modules.activiti.controller.oa.vo.OaLeaveCreateReqVO;
import cn.iocoder.yudao.adminserver.modules.activiti.controller.oa.vo.OaLeaveExportReqVO;
import cn.iocoder.yudao.adminserver.modules.activiti.controller.oa.vo.OaLeavePageReqVO;
import cn.iocoder.yudao.adminserver.modules.activiti.controller.oa.vo.OaLeaveUpdateReqVO;
import cn.iocoder.yudao.adminserver.modules.activiti.dal.dataobject.oa.OaLeaveDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 请假申请 Service 接口
 *
 * @author 芋艿
 */
public interface OaLeaveService {

    /**
     * 创建请假申请
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createLeave(@Valid OaLeaveCreateReqVO createReqVO);

    /**
     * 更新请假申请
     *
     * @param updateReqVO 更新信息
     */
    void updateLeave(@Valid OaLeaveUpdateReqVO updateReqVO);

    /**
     * 删除请假申请
     *
     * @param id 编号
     */
    void deleteLeave(Long id);

    /**
     * 获得请假申请
     *
     * @param id 编号
     * @return 请假申请
     */
    OaLeaveDO getLeave(Long id);

    /**
     * 获得请假申请列表
     *
     * @param ids 编号
     * @return 请假申请列表
     */
    List<OaLeaveDO> getLeaveList(Collection<Long> ids);

    /**
     * 获得请假申请分页
     *
     * @param pageReqVO 分页查询
     * @return 请假申请分页
     */
    PageResult<OaLeaveDO> getLeavePage(OaLeavePageReqVO pageReqVO);

    /**
     * 获得请假申请列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 请假申请列表
     */
    List<OaLeaveDO> getLeaveList(OaLeaveExportReqVO exportReqVO);

}
