package cn.iocoder.yudao.module.mes.service.pro.card;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.pro.card.vo.MesProCardPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.pro.card.vo.MesProCardSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.card.MesProCardDO;
import jakarta.validation.Valid;

/**
 * MES 生产流转卡 Service 接口
 *
 * @author 芋道源码
 */
public interface MesProCardService {

    /**
     * 创建生产流转卡
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCard(@Valid MesProCardSaveReqVO createReqVO);

    /**
     * 更新生产流转卡
     *
     * @param updateReqVO 更新信息
     */
    void updateCard(@Valid MesProCardSaveReqVO updateReqVO);

    /**
     * 删除生产流转卡
     *
     * @param id 编号
     */
    void deleteCard(Long id);

    /**
     * 校验生产流转卡存在
     *
     * @param id 编号
     */
    void validateCardExists(Long id);

    /**
     * 校验生产流转卡存在，且为草稿状态
     *
     * @param id 编号
     */
    void validateCardExistsAndPrepare(Long id);

    /**
     * 获得生产流转卡
     *
     * @param id 编号
     * @return 生产流转卡
     */
    MesProCardDO getCard(Long id);

    /**
     * 获得生产流转卡分页
     *
     * @param pageReqVO 分页查询
     * @return 生产流转卡分页
     */
    PageResult<MesProCardDO> getCardPage(MesProCardPageReqVO pageReqVO);

    /**
     * 提交生产流转卡（草稿 → 已确认）
     *
     * @param id 编号
     */
    void submitCard(Long id);

    /**
     * 完成生产流转卡（已确认 → 已完成）
     *
     * @param id 编号
     */
    void finishCard(Long id);

    /**
     * 取消生产流转卡（任意非已完成/已取消状态 → 已取消）
     *
     * @param id 编号
     */
    void cancelCard(Long id);

}
