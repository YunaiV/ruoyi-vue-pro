package cn.iocoder.yudao.module.mes.service.wm.salesnotice;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.salesnotice.vo.MesWmSalesNoticePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.salesnotice.vo.MesWmSalesNoticeSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.salesnotice.MesWmSalesNoticeDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.salesnotice.MesWmSalesNoticeLineDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.salesnotice.MesWmSalesNoticeMapper;
import cn.iocoder.yudao.module.mes.service.md.client.MesMdClientService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.mes.enums.wm.MesWmSalesNoticeStatusEnum.*;

/**
 * MES 发货通知单 Service 实现类
 */
@Service
@Validated
@Slf4j
public class MesWmSalesNoticeServiceImpl implements MesWmSalesNoticeService {

    @Resource
    private MesWmSalesNoticeMapper salesNoticeMapper;

    @Resource
    private MesWmSalesNoticeLineService salesNoticeLineService;
    @Resource
    private MesMdClientService clientService;

    @Override
    public Long createSalesNotice(MesWmSalesNoticeSaveReqVO createReqVO) {
        // 校验数据
        validateSalesNoticeSave(createReqVO);

        // 插入
        MesWmSalesNoticeDO notice = BeanUtils.toBean(createReqVO, MesWmSalesNoticeDO.class);
        notice.setStatus(PREPARE.getStatus()); // 草稿状态
        salesNoticeMapper.insert(notice);
        return notice.getId();
    }

    @Override
    public void updateSalesNotice(MesWmSalesNoticeSaveReqVO updateReqVO) {
        // 校验存在 + 草稿状态
        validateSalesNoticeExistsAndDraft(updateReqVO.getId());
        // 校验数据
        validateSalesNoticeSave(updateReqVO);

        // 更新
        MesWmSalesNoticeDO updateObj = BeanUtils.toBean(updateReqVO, MesWmSalesNoticeDO.class);
        salesNoticeMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSalesNotice(Long id) {
        // 校验存在 + 草稿状态
        validateSalesNoticeExistsAndDraft(id);

        // 级联删除行
        salesNoticeLineService.deleteSalesNoticeLineByNoticeId(id);
        // 删除
        salesNoticeMapper.deleteById(id);
    }

    @Override
    public MesWmSalesNoticeDO getSalesNotice(Long id) {
        return salesNoticeMapper.selectById(id);
    }

    @Override
    public PageResult<MesWmSalesNoticeDO> getSalesNoticePage(MesWmSalesNoticePageReqVO pageReqVO) {
        return salesNoticeMapper.selectPage(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitSalesNotice(Long id) {
        // 1.1 校验存在 + 草稿状态
        validateSalesNoticeExistsAndDraft(id);
        // 1.2 检查是否有行项目
        List<MesWmSalesNoticeLineDO> lines = salesNoticeLineService.getSalesNoticeLineListByNoticeId(id);
        if (CollUtil.isEmpty(lines)) {
            throw exception(WM_SALES_NOTICE_LINE_EMPTY);
        }

        // 2. 更新状态：草稿 → 待出库
        salesNoticeMapper.updateById(new MesWmSalesNoticeDO()
                .setId(id).setStatus(APPROVED.getStatus()));
    }

    @Override
    public List<MesWmSalesNoticeDO> getSalesNoticeList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return salesNoticeMapper.selectByIds(ids);
    }

    @Override
    public MesWmSalesNoticeDO validateSalesNoticeExists(Long id) {
        MesWmSalesNoticeDO notice = salesNoticeMapper.selectById(id);
        if (notice == null) {
            throw exception(WM_SALES_NOTICE_NOT_EXISTS);
        }
        return notice;
    }

    /**
     * 校验发货通知单存在且为草稿状态
     */
    private MesWmSalesNoticeDO validateSalesNoticeExistsAndDraft(Long id) {
        MesWmSalesNoticeDO notice = validateSalesNoticeExists(id);
        if (ObjUtil.notEqual(PREPARE.getStatus(), notice.getStatus())) {
            throw exception(WM_SALES_NOTICE_STATUS_NOT_ALLOW_DELETE);
        }
        return notice;
    }

    private void validateSalesNoticeSave(MesWmSalesNoticeSaveReqVO saveReqVO) {
        // 校验编码唯一
        validateNoticeCodeUnique(saveReqVO.getId(), saveReqVO.getCode());
        // 校验客户存在
        if (saveReqVO.getClientId() != null) {
            clientService.validateClientExistsAndEnable(saveReqVO.getClientId());
        }
    }

    private void validateNoticeCodeUnique(Long id, String code) {
        MesWmSalesNoticeDO notice = salesNoticeMapper.selectByCode(code);
        if (notice == null) {
            return;
        }
        if (ObjUtil.notEqual(id, notice.getId())) {
            throw exception(WM_SALES_NOTICE_CODE_DUPLICATE);
        }
    }

}
