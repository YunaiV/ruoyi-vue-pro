package cn.iocoder.yudao.module.mes.service.pro.card;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.pro.card.vo.process.MesProCardProcessPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.pro.card.vo.process.MesProCardProcessSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.card.MesProCardProcessDO;
import cn.iocoder.yudao.module.mes.dal.mysql.pro.card.MesProCardProcessMapper;
import cn.iocoder.yudao.module.mes.service.pro.process.MesProProcessService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.PRO_CARD_PROCESS_NOT_EXISTS;

/**
 * MES 流转卡工序记录 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesProCardProcessServiceImpl implements MesProCardProcessService {

    @Resource
    private MesProCardProcessMapper cardProcessMapper;

    @Resource
    @Lazy
    private MesProCardService cardService;
    @Resource
    private MesProProcessService processService;

    @Override
    public Long createCardProcess(MesProCardProcessSaveReqVO createReqVO) {
        // 1. 校验关联数据（含流转卡草稿状态校验）
        validateCardProcessSaveData(createReqVO);

        // 2. 插入
        MesProCardProcessDO cardProcess = BeanUtils.toBean(createReqVO, MesProCardProcessDO.class);
        cardProcessMapper.insert(cardProcess);
        return cardProcess.getId();
    }

    @Override
    public void updateCardProcess(MesProCardProcessSaveReqVO updateReqVO) {
        // 1.1 校验存在
        validateCardProcessExists(updateReqVO.getId());
        // 1.2 校验关联数据（含流转卡草稿状态校验）
        validateCardProcessSaveData(updateReqVO);

        // 2. 更新
        MesProCardProcessDO updateObj = BeanUtils.toBean(updateReqVO, MesProCardProcessDO.class);
        cardProcessMapper.updateById(updateObj);
    }

    @Override
    public void deleteCardProcess(Long id) {
        // 1. 校验存在
        MesProCardProcessDO cardProcess = cardProcessMapper.selectById(id);
        if (cardProcess == null) {
            throw exception(PRO_CARD_PROCESS_NOT_EXISTS);
        }
        // 2. 校验流转卡为草稿状态
        cardService.validateCardExistsAndPrepare(cardProcess.getCardId());

        // 3. 删除
        cardProcessMapper.deleteById(id);
    }

    @Override
    public MesProCardProcessDO getCardProcess(Long id) {
        return cardProcessMapper.selectById(id);
    }

    @Override
    public PageResult<MesProCardProcessDO> getCardProcessPage(MesProCardProcessPageReqVO pageReqVO) {
        return cardProcessMapper.selectPage(pageReqVO);
    }

    // ==================== 校验方法 ====================

    private void validateCardProcessExists(Long id) {
        if (cardProcessMapper.selectById(id) == null) {
            throw exception(PRO_CARD_PROCESS_NOT_EXISTS);
        }
    }

    private void validateCardProcessSaveData(MesProCardProcessSaveReqVO reqVO) {
        // 校验流转卡存在 + 草稿状态
        cardService.validateCardExistsAndPrepare(reqVO.getCardId());
        // 校验工序存在
        if (reqVO.getProcessId() != null) {
            processService.validateProcessExistsAndEnable(reqVO.getProcessId());
        }
    }

    @Override
    public void deleteCardProcessByCardId(Long cardId) {
        cardProcessMapper.deleteByCardId(cardId);
    }

}
