package cn.iocoder.yudao.module.jl.service.join;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.jl.controller.admin.join.vo.*;
import cn.iocoder.yudao.module.jl.dal.dataobject.join.JoinSaleslead2competitorDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 销售线索中竞争对手的报价 Service 接口
 *
 * @author 惟象科技
 */
public interface JoinSaleslead2competitorService {

    /**
     * 创建销售线索中竞争对手的报价
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createJoinSaleslead2competitor(@Valid JoinSaleslead2competitorCreateReqVO createReqVO);

    /**
     * 更新销售线索中竞争对手的报价
     *
     * @param updateReqVO 更新信息
     */
    void updateJoinSaleslead2competitor(@Valid JoinSaleslead2competitorUpdateReqVO updateReqVO);

    /**
     * 删除销售线索中竞争对手的报价
     *
     * @param id 编号
     */
    void deleteJoinSaleslead2competitor(Long id);

    /**
     * 获得销售线索中竞争对手的报价
     *
     * @param id 编号
     * @return 销售线索中竞争对手的报价
     */
    JoinSaleslead2competitorDO getJoinSaleslead2competitor(Long id);

    /**
     * 获得销售线索中竞争对手的报价列表
     *
     * @param ids 编号
     * @return 销售线索中竞争对手的报价列表
     */
    List<JoinSaleslead2competitorDO> getJoinSaleslead2competitorList(Collection<Long> ids);

    /**
     * 获得销售线索中竞争对手的报价分页
     *
     * @param pageReqVO 分页查询
     * @return 销售线索中竞争对手的报价分页
     */
    PageResult<JoinSaleslead2competitorDO> getJoinSaleslead2competitorPage(JoinSaleslead2competitorPageReqVO pageReqVO);

    /**
     * 获得销售线索中竞争对手的报价列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 销售线索中竞争对手的报价列表
     */
    List<JoinSaleslead2competitorDO> getJoinSaleslead2competitorList(JoinSaleslead2competitorExportReqVO exportReqVO);

}
