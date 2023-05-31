package cn.iocoder.yudao.module.jl.service.crm;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.*;
import cn.iocoder.yudao.module.jl.entity.crm.Followup;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 销售跟进 Service 接口
 *
 */
public interface FollowupService {

    /**
     * 创建销售跟进
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createFollowup(@Valid FollowupCreateReqVO createReqVO);

    /**
     * 更新销售跟进
     *
     * @param updateReqVO 更新信息
     */
    void updateFollowup(@Valid FollowupUpdateReqVO updateReqVO);

    /**
     * 删除销售跟进
     *
     * @param id 编号
     */
    void deleteFollowup(Long id);

    /**
     * 获得销售跟进
     *
     * @param id 编号
     * @return 销售跟进
     */
    Optional<Followup> getFollowup(Long id);

    /**
     * 获得销售跟进列表
     *
     * @param ids 编号
     * @return 销售跟进列表
     */
    List<Followup> getFollowupList(Collection<Long> ids);

    /**
     * 获得销售跟进分页
     *
     * @param pageReqVO 分页查询
     * @return 销售跟进分页
     */
    PageResult<Followup> getFollowupPage(FollowupPageReqVO pageReqVO, FollowupPageOrder orderV0);

    /**
     * 获得销售跟进列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 销售跟进列表
     */
    List<Followup> getFollowupList(FollowupExportReqVO exportReqVO);

}
