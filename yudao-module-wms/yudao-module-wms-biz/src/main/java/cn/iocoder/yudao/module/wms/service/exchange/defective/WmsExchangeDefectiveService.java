package cn.iocoder.yudao.module.wms.service.exchange.defective;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.wms.controller.admin.exchange.defective.vo.WmsExchangeDefectivePageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.exchange.defective.vo.WmsExchangeDefectiveSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.exchange.defective.WmsExchangeDefectiveDO;
import jakarta.validation.Valid;
import java.util.List;

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
    WmsExchangeDefectiveDO createExchangeDefective(@Valid WmsExchangeDefectiveSaveReqVO createReqVO);

    /**
     * 更新良次换货详情
     *
     * @param updateReqVO 更新信息
     */
    WmsExchangeDefectiveDO updateExchangeDefective(@Valid WmsExchangeDefectiveSaveReqVO updateReqVO);

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

    /**
     * 按 ID 集合查询 WmsExchangeDefectiveDO
     */
    List<WmsExchangeDefectiveDO> selectByIds(List<Long> idList);

    List<WmsExchangeDefectiveDO> selectByExchangeId(Long id);
}
