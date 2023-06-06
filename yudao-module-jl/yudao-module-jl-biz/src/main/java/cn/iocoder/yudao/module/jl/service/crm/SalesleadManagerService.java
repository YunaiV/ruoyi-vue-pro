package cn.iocoder.yudao.module.jl.service.crm;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.*;
import cn.iocoder.yudao.module.jl.entity.crm.SalesleadManager;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 销售线索中的项目售前支持人员 Service 接口
 *
 */
public interface SalesleadManagerService {

    /**
     * 创建销售线索中的项目售前支持人员
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSalesleadManager(@Valid SalesleadManagerCreateReqVO createReqVO);

    /**
     * 更新销售线索中的项目售前支持人员
     *
     * @param updateReqVO 更新信息
     */
    void updateSalesleadManager(@Valid SalesleadManagerUpdateReqVO updateReqVO);

    /**
     * 删除销售线索中的项目售前支持人员
     *
     * @param id 编号
     */
    void deleteSalesleadManager(Long id);

    /**
     * 获得销售线索中的项目售前支持人员
     *
     * @param id 编号
     * @return 销售线索中的项目售前支持人员
     */
    Optional<SalesleadManager> getSalesleadManager(Long id);

    /**
     * 获得销售线索中的项目售前支持人员列表
     *
     * @param ids 编号
     * @return 销售线索中的项目售前支持人员列表
     */
    List<SalesleadManager> getSalesleadManagerList(Collection<Long> ids);

    /**
     * 获得销售线索中的项目售前支持人员分页
     *
     * @param pageReqVO 分页查询
     * @return 销售线索中的项目售前支持人员分页
     */
    PageResult<SalesleadManager> getSalesleadManagerPage(SalesleadManagerPageReqVO pageReqVO, SalesleadManagerPageOrder orderV0);

    /**
     * 获得销售线索中的项目售前支持人员列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 销售线索中的项目售前支持人员列表
     */
    List<SalesleadManager> getSalesleadManagerList(SalesleadManagerExportReqVO exportReqVO);

}
