package cn.iocoder.yudao.module.mes.service.md.workstation;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.md.workstation.vo.workshop.MesMdWorkshopPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.workstation.vo.workshop.MesMdWorkshopSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.workstation.MesMdWorkshopDO;
import cn.iocoder.yudao.module.mes.dal.mysql.md.workstation.MesMdWorkshopMapper;
import cn.iocoder.yudao.module.mes.enums.wm.BarcodeBizTypeEnum;
import cn.iocoder.yudao.module.mes.service.wm.barcode.MesWmBarcodeService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 车间 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesMdWorkshopServiceImpl implements MesMdWorkshopService {

    @Resource
    private MesMdWorkshopMapper workshopMapper;

    @Resource
    @Lazy
    private MesMdWorkstationService workstationService;
    @Resource
    private MesWmBarcodeService barcodeService;

    @Override
    public Long createWorkshop(MesMdWorkshopSaveReqVO createReqVO) {
        // 校验编码唯一
        validateWorkshopCodeUnique(null, createReqVO.getCode());
        // 校验名称唯一
        validateWorkshopNameUnique(null, createReqVO.getName());

        // 插入
        MesMdWorkshopDO workshop = BeanUtils.toBean(createReqVO, MesMdWorkshopDO.class);
        workshopMapper.insert(workshop);

        // 自动生成条码
        barcodeService.autoGenerateBarcode(BarcodeBizTypeEnum.WORKSHOP.getValue(),
                workshop.getId(), workshop.getCode(), workshop.getName());
        return workshop.getId();
    }

    @Override
    public void updateWorkshop(MesMdWorkshopSaveReqVO updateReqVO) {
        // 校验存在
        validateWorkshopExists(updateReqVO.getId());
        // 校验编码唯一
        validateWorkshopCodeUnique(updateReqVO.getId(), updateReqVO.getCode());
        // 校验名称唯一
        validateWorkshopNameUnique(updateReqVO.getId(), updateReqVO.getName());

        // 更新
        MesMdWorkshopDO updateObj = BeanUtils.toBean(updateReqVO, MesMdWorkshopDO.class);
        workshopMapper.updateById(updateObj);
    }

    @Override
    public void deleteWorkshop(Long id) {
        // 校验存在
        validateWorkshopExists(id);
        // 校验车间下是否存在工作站
        Long count = workstationService.getWorkstationCountByWorkshopId(id);
        if (count > 0) {
            throw exception(MD_WORKSHOP_HAS_WORKSTATION);
        }

        // 删除
        workshopMapper.deleteById(id);
    }

    private void validateWorkshopExists(Long id) {
        if (workshopMapper.selectById(id) == null) {
            throw exception(MD_WORKSHOP_NOT_EXISTS);
        }
    }

    private void validateWorkshopCodeUnique(Long id, String code) {
        MesMdWorkshopDO workshop = workshopMapper.selectByCode(code);
        if (workshop == null) {
            return;
        }
        if (ObjUtil.notEqual(workshop.getId(), id)) {
            throw exception(MD_WORKSHOP_CODE_DUPLICATE);
        }
    }

    private void validateWorkshopNameUnique(Long id, String name) {
        MesMdWorkshopDO workshop = workshopMapper.selectByName(name);
        if (workshop == null) {
            return;
        }
        if (ObjUtil.notEqual(workshop.getId(), id)) {
            throw exception(MD_WORKSHOP_NAME_DUPLICATE);
        }
    }

    @Override
    public MesMdWorkshopDO getWorkshop(Long id) {
        return workshopMapper.selectById(id);
    }

    @Override
    public PageResult<MesMdWorkshopDO> getWorkshopPage(MesMdWorkshopPageReqVO pageReqVO) {
        return workshopMapper.selectPage(pageReqVO);
    }

    @Override
    public List<MesMdWorkshopDO> getWorkshopList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return workshopMapper.selectByIds(ids);
    }

    @Override
    public List<MesMdWorkshopDO> getWorkshopListByStatus(Integer status) {
        return workshopMapper.selectListByStatus(status);
    }

}
