package cn.iocoder.yudao.module.jl.service.crm;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.*;
import cn.iocoder.yudao.module.jl.dal.dataobject.crm.FollowupDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 销售线索跟进，可以是跟进客户，也可以是跟进线索 Service 接口
 *
 * @author 惟象科技
 */
public interface FollowupService {

    /**
     * 创建销售线索跟进，可以是跟进客户，也可以是跟进线索
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createFollowup(@Valid FollowupCreateReqVO createReqVO);

    /**
     * 更新销售线索跟进，可以是跟进客户，也可以是跟进线索
     *
     * @param updateReqVO 更新信息
     */
    void updateFollowup(@Valid FollowupUpdateReqVO updateReqVO);

    /**
     * 删除销售线索跟进，可以是跟进客户，也可以是跟进线索
     *
     * @param id 编号
     */
    void deleteFollowup(Long id);

    /**
     * 获得销售线索跟进，可以是跟进客户，也可以是跟进线索
     *
     * @param id 编号
     * @return 销售线索跟进，可以是跟进客户，也可以是跟进线索
     */
    FollowupDO getFollowup(Long id);

    FollowupDO selectLatestOneBySealsLeadId(Long id);

    /**
     * 获得销售线索跟进，可以是跟进客户，也可以是跟进线索列表
     *
     * @param ids 编号
     * @return 销售线索跟进，可以是跟进客户，也可以是跟进线索列表
     */
    List<FollowupDO> getFollowupList(Collection<Long> ids);

    /**
     * 获得销售线索跟进，可以是跟进客户，也可以是跟进线索分页
     *
     * @param pageReqVO 分页查询
     * @return 销售线索跟进，可以是跟进客户，也可以是跟进线索分页
     */
    PageResult<FollowupDO> getFollowupPage(FollowupPageReqVO pageReqVO);

    /**
     * 获得销售线索跟进，可以是跟进客户，也可以是跟进线索列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 销售线索跟进，可以是跟进客户，也可以是跟进线索列表
     */
    List<FollowupDO> getFollowupList(FollowupExportReqVO exportReqVO);

}
