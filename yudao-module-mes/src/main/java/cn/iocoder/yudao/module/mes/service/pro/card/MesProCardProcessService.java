package cn.iocoder.yudao.module.mes.service.pro.card;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.pro.card.vo.process.MesProCardProcessPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.pro.card.vo.process.MesProCardProcessSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.card.MesProCardProcessDO;
import jakarta.validation.Valid;

/**
 * MES 流转卡工序记录 Service 接口
 *
 * @author 芋道源码
 */
public interface MesProCardProcessService {

    /**
     * 创建流转卡工序记录
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCardProcess(@Valid MesProCardProcessSaveReqVO createReqVO);

    /**
     * 更新流转卡工序记录
     *
     * @param updateReqVO 更新信息
     */
    void updateCardProcess(@Valid MesProCardProcessSaveReqVO updateReqVO);

    /**
     * 删除流转卡工序记录
     *
     * @param id 编号
     */
    void deleteCardProcess(Long id);

    /**
     * 获得流转卡工序记录
     *
     * @param id 编号
     * @return 流转卡工序记录
     */
    MesProCardProcessDO getCardProcess(Long id);

    /**
     * 获得流转卡工序记录分页
     *
     * @param pageReqVO 分页查询
     * @return 流转卡工序记录分页
     */
    PageResult<MesProCardProcessDO> getCardProcessPage(MesProCardProcessPageReqVO pageReqVO);

    /**
     * 根据流转卡编号，删除工序记录
     *
     * @param cardId 流转卡编号
     */
    void deleteCardProcessByCardId(Long cardId);

}
