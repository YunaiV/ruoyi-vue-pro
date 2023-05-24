package cn.iocoder.yudao.module.jl.service.join;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.jl.controller.admin.join.vo.*;
import cn.iocoder.yudao.module.jl.dal.dataobject.join.JoinSaleslead2customerplanDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.jl.dal.dataobject.join.JoinSaleslead2reportDO;

/**
 * 销售线索中的客户方案 Service 接口
 *
 * @author 惟象科技
 */
public interface JoinSaleslead2customerplanService {

    /**
     * 创建销售线索中的客户方案
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createJoinSaleslead2customerplan(@Valid JoinSaleslead2customerplanCreateReqVO createReqVO);

    /**
     * 根据销售id，获得销售线索中的方案
     *
     * @param id 方案id
     * @return 销售线索中的方案
     */
    List<JoinSaleslead2customerplanDO> getCustomerPlanBySalesleadId(Long id);

    /**
     * 更新销售线索中的客户方案
     *
     * @param updateReqVO 更新信息
     */
    void updateJoinSaleslead2customerplan(@Valid JoinSaleslead2customerplanUpdateReqVO updateReqVO);

    /**
     * 删除销售线索中的客户方案
     *
     * @param id 编号
     */
    void deleteJoinSaleslead2customerplan(Long id);

    /**
     * 获得销售线索中的客户方案
     *
     * @param id 编号
     * @return 销售线索中的客户方案
     */
    JoinSaleslead2customerplanDO getJoinSaleslead2customerplan(Long id);

    /**
     * 获得销售线索中的客户方案列表
     *
     * @param ids 编号
     * @return 销售线索中的客户方案列表
     */
    List<JoinSaleslead2customerplanDO> getJoinSaleslead2customerplanList(Collection<Long> ids);

    /**
     * 获得销售线索中的客户方案分页
     *
     * @param pageReqVO 分页查询
     * @return 销售线索中的客户方案分页
     */
    PageResult<JoinSaleslead2customerplanDO> getJoinSaleslead2customerplanPage(JoinSaleslead2customerplanPageReqVO pageReqVO);

    /**
     * 获得销售线索中的客户方案列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 销售线索中的客户方案列表
     */
    List<JoinSaleslead2customerplanDO> getJoinSaleslead2customerplanList(JoinSaleslead2customerplanExportReqVO exportReqVO);

}
