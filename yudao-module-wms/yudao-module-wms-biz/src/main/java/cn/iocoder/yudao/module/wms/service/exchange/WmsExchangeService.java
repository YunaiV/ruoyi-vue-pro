package cn.iocoder.yudao.module.wms.service.exchange;

import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.wms.controller.admin.exchange.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.exchange.WmsExchangeDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 换货单 Service 接口
 *
 * @author 李方捷
 */
public interface WmsExchangeService {

    /**
     * 创建换货单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createExchange(@Valid WmsExchangeSaveReqVO createReqVO);

    /**
     * 更新换货单
     *
     * @param updateReqVO 更新信息
     */
    void updateExchange(@Valid WmsExchangeSaveReqVO updateReqVO);

    /**
     * 删除换货单
     *
     * @param id 编号
     */
    void deleteExchange(Long id);

    /**
     * 获得换货单
     *
     * @param id 编号
     * @return 换货单
     */
    WmsExchangeDO getExchange(Long id);

    /**
     * 获得换货单分页
     *
     * @param pageReqVO 分页查询
     * @return 换货单分页
     */
    PageResult<WmsExchangeDO> getExchangePage(WmsExchangePageReqVO pageReqVO);

}