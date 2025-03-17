package cn.iocoder.yudao.module.wms.service.outbound;

import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.outbound.WmsOutboundDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 出库单 Service 接口
 *
 * @author 李方捷
 */
public interface WmsOutboundService {

    /**
     * 创建出库单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createOutbound(@Valid WmsOutboundSaveReqVO createReqVO);

    /**
     * 更新出库单
     *
     * @param updateReqVO 更新信息
     */
    void updateOutbound(@Valid WmsOutboundSaveReqVO updateReqVO);

    /**
     * 删除出库单
     *
     * @param id 编号
     */
    void deleteOutbound(Long id);

    /**
     * 获得出库单
     *
     * @param id 编号
     * @return 出库单
     */
    WmsOutboundDO getOutbound(Long id);

    /**
     * 获得出库单分页
     *
     * @param pageReqVO 分页查询
     * @return 出库单分页
     */
    PageResult<WmsOutboundDO> getOutboundPage(WmsOutboundPageReqVO pageReqVO);

}