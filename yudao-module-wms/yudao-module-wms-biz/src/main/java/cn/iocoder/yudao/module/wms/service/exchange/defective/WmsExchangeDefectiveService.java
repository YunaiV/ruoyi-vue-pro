package cn.iocoder.yudao.module.wms.service.exchange.defective;

import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.wms.controller.admin.exchange.defective.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.exchange.defective.WmsExchangeDefectiveDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 良次换货详情 Service 接口
 *
 * @author 李方捷
 */
public interface WmsExchangeDefectiveService {

    /**
     * 创建良次换货详情
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createExchangeDefective(@Valid WmsExchangeDefectiveSaveReqVO createReqVO);

    /**
     * 更新良次换货详情
     *
     * @param updateReqVO 更新信息
     */
    void updateExchangeDefective(@Valid WmsExchangeDefectiveSaveReqVO updateReqVO);

    /**
     * 删除良次换货详情
     *
     * @param id 编号
     */
    void deleteExchangeDefective(Long id);

    /**
     * 获得良次换货详情
     *
     * @param id 编号
     * @return 良次换货详情
     */
    WmsExchangeDefectiveDO getExchangeDefective(Long id);

    /**
     * 获得良次换货详情分页
     *
     * @param pageReqVO 分页查询
     * @return 良次换货详情分页
     */
    PageResult<WmsExchangeDefectiveDO> getExchangeDefectivePage(WmsExchangeDefectivePageReqVO pageReqVO);

}