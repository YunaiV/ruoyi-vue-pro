package cn.iocoder.yudao.module.mes.service.pro.card;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.module.mes.controller.admin.pro.card.vo.MesProCardPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.pro.card.vo.MesProCardSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.card.MesProCardDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.workorder.MesProWorkOrderDO;
import cn.iocoder.yudao.module.mes.dal.mysql.pro.card.MesProCardMapper;
import cn.iocoder.yudao.module.mes.enums.pro.MesProWorkOrderStatusEnum;
import cn.iocoder.yudao.module.mes.enums.wm.BarcodeBizTypeEnum;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.pro.workorder.MesProWorkOrderService;
import cn.iocoder.yudao.module.mes.service.wm.barcode.MesWmBarcodeService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 生产流转卡 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesProCardServiceImpl implements MesProCardService {

    @Resource
    private MesProCardMapper cardMapper;

    @Resource
    @Lazy
    private MesProCardProcessService cardProcessService;
    @Resource
    private MesProWorkOrderService workOrderService;
    @Resource
    private MesMdItemService itemService;
    @Resource
    private MesWmBarcodeService barcodeService;

    @Override
    public Long createCard(MesProCardSaveReqVO createReqVO) {
        // 1. 校验关联数据
        validateCardSaveData(createReqVO);

        // 2. 插入
        MesProCardDO card = BeanUtils.toBean(createReqVO, MesProCardDO.class)
                .setStatus(MesProWorkOrderStatusEnum.PREPARE.getStatus());
        cardMapper.insert(card);

        // 3. 自动生成条码
        barcodeService.autoGenerateBarcode(BarcodeBizTypeEnum.PROCARD.getValue(),
                card.getId(), card.getCode(), card.getCode());
        return card.getId();
    }

    @Override
    public void updateCard(MesProCardSaveReqVO updateReqVO) {
        // 1.1 校验存在 + 草稿状态
        validateCardExistsAndPrepare(updateReqVO.getId());
        // 1.2 校验关联数据
        validateCardSaveData(updateReqVO);

        // 2. 更新
        MesProCardDO updateObj = BeanUtils.toBean(updateReqVO, MesProCardDO.class);
        cardMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCard(Long id) {
        // 1. 校验存在 + 草稿状态
        validateCardExistsAndPrepare(id);

        // 2. 删除流转卡 + 级联删除工序记录
        cardMapper.deleteById(id);
        cardProcessService.deleteCardProcessByCardId(id);
    }

    @Override
    public MesProCardDO getCard(Long id) {
        return cardMapper.selectById(id);
    }

    @Override
    public PageResult<MesProCardDO> getCardPage(MesProCardPageReqVO pageReqVO) {
        return cardMapper.selectPage(pageReqVO);
    }

    @Override
    public void submitCard(Long id) {
        // 1. 校验存在 + 草稿状态
        validateCardExistsAndStatus(id, MesProWorkOrderStatusEnum.PREPARE);

        // 2. 提交（草稿 → 已确认）
        cardMapper.updateById(new MesProCardDO()
                .setId(id).setStatus(MesProWorkOrderStatusEnum.CONFIRMED.getStatus()));
    }

    @Override
    public void finishCard(Long id) {
        // 1. 校验存在 + 已确认状态
        validateCardExistsAndStatus(id, MesProWorkOrderStatusEnum.CONFIRMED);

        // 2. 完成（已确认 → 已完成）
        cardMapper.updateById(new MesProCardDO()
                .setId(id).setStatus(MesProWorkOrderStatusEnum.FINISHED.getStatus()));
    }

    @Override
    public void cancelCard(Long id) {
        // 1.1 校验存在
        MesProCardDO card = validateCardExistsInternal(id);
        // 1.2 已完成和已取消不允许取消
        if (ObjectUtils.equalsAny(card.getStatus(),
                MesProWorkOrderStatusEnum.FINISHED.getStatus(),
                MesProWorkOrderStatusEnum.CANCELED.getStatus())) {
            throw exception(PRO_CARD_CANCEL_NOT_ALLOWED);
        }

        // 2. 取消
        cardMapper.updateById(new MesProCardDO()
                .setId(id).setStatus(MesProWorkOrderStatusEnum.CANCELED.getStatus()));
    }

    // ==================== 校验方法 ====================

    @Override
    public void validateCardExists(Long id) {
        validateCardExistsInternal(id);
    }

    @Override
    public void validateCardExistsAndPrepare(Long id) {
        MesProCardDO card = validateCardExistsInternal(id);
        if (ObjUtil.notEqual(MesProWorkOrderStatusEnum.PREPARE.getStatus(), card.getStatus())) {
            throw exception(PRO_CARD_NOT_PREPARE);
        }
    }

    private MesProCardDO validateCardExistsInternal(Long id) {
        MesProCardDO card = cardMapper.selectById(id);
        if (card == null) {
            throw exception(PRO_CARD_NOT_EXISTS);
        }
        return card;
    }

    private MesProCardDO validateCardExistsAndStatus(Long id, MesProWorkOrderStatusEnum status) {
        MesProCardDO card = validateCardExistsInternal(id);
        if (ObjUtil.notEqual(status.getStatus(), card.getStatus())) {
            throw exception(PRO_CARD_STATUS_ERROR);
        }
        return card;
    }

    private void validateCardCodeUnique(Long id, String code) {
        if (code == null) {
            return;
        }
        MesProCardDO card = cardMapper.selectByCode(code);
        if (card == null) {
            return;
        }
        if (ObjUtil.notEqual(card.getId(), id)) {
            throw exception(PRO_CARD_CODE_DUPLICATE);
        }
    }

    private void validateCardSaveData(MesProCardSaveReqVO reqVO) {
        // 校验编码唯一
        validateCardCodeUnique(reqVO.getId(), reqVO.getCode());
        // 校验工单存在
        MesProWorkOrderDO workOrder = workOrderService.validateWorkOrderConfirmed(reqVO.getWorkOrderId());
        // 校验物料存在
        itemService.validateItemExists(reqVO.getItemId());
        if (ObjUtil.notEqual(workOrder.getProductId(), reqVO.getItemId())) {
            throw exception(PRO_WORK_ORDER_PRODUCT_MISMATCH);
        }
    }

}
