package cn.iocoder.yudao.module.jl.service.join;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.jl.controller.admin.join.vo.*;
import cn.iocoder.yudao.module.jl.dal.dataobject.join.JoinSaleslead2reportDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 销售线索中的方案 Service 接口
 *
 * @author 惟象科技
 */
public interface JoinSaleslead2reportService {

    /**
     * 创建销售线索中的方案
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createJoinSaleslead2report(@Valid JoinSaleslead2reportCreateReqVO createReqVO);

    /**
     * 更新销售线索中的方案
     *
     * @param updateReqVO 更新信息
     */
    void updateJoinSaleslead2report(@Valid JoinSaleslead2reportUpdateReqVO updateReqVO);

    /**
     * 删除销售线索中的方案
     *
     * @param id 编号
     */
    void deleteJoinSaleslead2report(Long id);

    /**
     * 获得销售线索中的方案
     *
     * @param id 编号
     * @return 销售线索中的方案
     */
    JoinSaleslead2reportDO getJoinSaleslead2report(Long id);

    /**
     * 获得销售线索中的方案列表
     *
     * @param ids 编号
     * @return 销售线索中的方案列表
     */
    List<JoinSaleslead2reportDO> getJoinSaleslead2reportList(Collection<Long> ids);

    /**
     * 获得销售线索中的方案分页
     *
     * @param pageReqVO 分页查询
     * @return 销售线索中的方案分页
     */
    PageResult<JoinSaleslead2reportDO> getJoinSaleslead2reportPage(JoinSaleslead2reportPageReqVO pageReqVO);

    /**
     * 获得销售线索中的方案列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 销售线索中的方案列表
     */
    List<JoinSaleslead2reportDO> getJoinSaleslead2reportList(JoinSaleslead2reportExportReqVO exportReqVO);

}
