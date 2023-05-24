package cn.iocoder.yudao.module.jl.service.join;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.jl.controller.admin.join.vo.*;
import cn.iocoder.yudao.module.jl.dal.dataobject.join.JoinSaleslead2managerDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 销售线索中的项目售前支持人员 Service 接口
 *
 * @author 惟象科技
 */
public interface JoinSaleslead2managerService {

    /**
     * 创建销售线索中的项目售前支持人员
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createJoinSaleslead2manager(@Valid JoinSaleslead2managerCreateReqVO createReqVO);

    /**
     * 更新销售线索中的项目售前支持人员
     *
     * @param updateReqVO 更新信息
     */
    void updateJoinSaleslead2manager(@Valid JoinSaleslead2managerUpdateReqVO updateReqVO);

    /**
     * 删除销售线索中的项目售前支持人员
     *
     * @param id 编号
     */
    void deleteJoinSaleslead2manager(Long id);

    /**
     * 获得销售线索中的项目售前支持人员
     *
     * @param id 编号
     * @return 销售线索中的项目售前支持人员
     */
    JoinSaleslead2managerDO getJoinSaleslead2manager(Long id);

    /**
     * 获得销售线索中的项目售前支持人员列表
     *
     * @param ids 编号
     * @return 销售线索中的项目售前支持人员列表
     */
    List<JoinSaleslead2managerDO> getJoinSaleslead2managerList(Collection<Long> ids);

    /**
     * 获得销售线索中的项目售前支持人员分页
     *
     * @param pageReqVO 分页查询
     * @return 销售线索中的项目售前支持人员分页
     */
    PageResult<JoinSaleslead2managerDO> getJoinSaleslead2managerPage(JoinSaleslead2managerPageReqVO pageReqVO);

    /**
     * 获得销售线索中的项目售前支持人员列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 销售线索中的项目售前支持人员列表
     */
    List<JoinSaleslead2managerDO> getJoinSaleslead2managerList(JoinSaleslead2managerExportReqVO exportReqVO);

    List<JoinSaleslead2managerDO> getBySalesleadId(Long salesleadId);
}
