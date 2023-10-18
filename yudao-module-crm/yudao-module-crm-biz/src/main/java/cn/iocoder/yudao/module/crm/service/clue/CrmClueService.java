package cn.iocoder.yudao.module.crm.service.clue;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.crm.controller.admin.clue.vo.*;
import cn.iocoder.yudao.module.crm.dal.dataobject.clue.CrmClueDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 线索 Service 接口
 *
 * @author Wanwan
 */
public interface CrmClueService {

    /**
     * 创建线索
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createClue(@Valid CrmClueCreateReqVO createReqVO);

    /**
     * 更新线索
     *
     * @param updateReqVO 更新信息
     */
    void updateClue(@Valid CrmClueUpdateReqVO updateReqVO);

    /**
     * 删除线索
     *
     * @param id 编号
     */
    void deleteClue(Long id);

    /**
     * 获得线索
     *
     * @param id 编号
     * @return 线索
     */
    CrmClueDO getClue(Long id);

    /**
     * 获得线索列表
     *
     * @param ids 编号
     * @return 线索列表
     */
    List<CrmClueDO> getClueList(Collection<Long> ids);

    /**
     * 获得线索分页
     *
     * @param pageReqVO 分页查询
     * @return 线索分页
     */
    PageResult<CrmClueDO> getCluePage(CrmCluePageReqVO pageReqVO);

    /**
     * 获得线索列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 线索列表
     */
    List<CrmClueDO> getClueList(CrmClueExportReqVO exportReqVO);

}
