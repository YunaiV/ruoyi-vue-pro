package cn.iocoder.yudao.module.mes.service.md.item;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.md.item.vo.sip.MesMdProductSipPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.item.vo.sip.MesMdProductSipSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdProductSipDO;
import cn.iocoder.yudao.module.mes.dal.mysql.md.item.MesMdProductSipMapper;
import cn.iocoder.yudao.module.mes.service.pro.process.MesProProcessService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 产品SIP Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesMdProductSipServiceImpl implements MesMdProductSipService {

    @Resource
    private MesMdProductSipMapper productSipMapper;

    @Resource
    private MesProProcessService processService;

    @Override
    public Long createProductSip(MesMdProductSipSaveReqVO createReqVO) {
        // 校验数据
        validateProductSipSaveData(createReqVO, null);

        // 插入
        MesMdProductSipDO sip = BeanUtils.toBean(createReqVO, MesMdProductSipDO.class);
        productSipMapper.insert(sip);
        return sip.getId();
    }

    @Override
    public void updateProductSip(MesMdProductSipSaveReqVO updateReqVO) {
        // 校验存在
        validateProductSipExists(updateReqVO.getId());
        // 校验数据
        validateProductSipSaveData(updateReqVO, updateReqVO.getId());

        // 更新
        MesMdProductSipDO updateObj = BeanUtils.toBean(updateReqVO, MesMdProductSipDO.class);
        productSipMapper.updateById(updateObj);
    }

    private void validateProductSipSaveData(MesMdProductSipSaveReqVO reqVO, Long excludeId) {
        // 校验排列顺序的唯一性
        validateSortUnique(reqVO.getItemId(), reqVO.getSort(), excludeId);
        // 校验工序存在
        if (reqVO.getProcessId() != null) {
            processService.validateProcessExistsAndEnable(reqVO.getProcessId());
        }
    }

    @Override
    public void deleteProductSip(Long id) {
        // 校验存在
        validateProductSipExists(id);
        // 删除
        productSipMapper.deleteById(id);
    }

    private void validateProductSipExists(Long id) {
        if (productSipMapper.selectById(id) == null) {
            throw exception(MD_PRODUCT_SIP_NOT_EXISTS);
        }
    }

    private void validateSortUnique(Long itemId, Integer sort, Long excludeId) {
        Long count = productSipMapper.selectCountByItemIdAndSort(itemId, sort, excludeId);
        if (count > 0) {
            throw exception(MD_PRODUCT_SIP_SORT_DUPLICATE);
        }
    }

    @Override
    public MesMdProductSipDO getProductSip(Long id) {
        return productSipMapper.selectById(id);
    }

    @Override
    public PageResult<MesMdProductSipDO> getProductSipPage(MesMdProductSipPageReqVO pageReqVO) {
        return productSipMapper.selectPage(pageReqVO);
    }

    @Override
    public List<MesMdProductSipDO> getProductSipListByItemId(Long itemId) {
        return productSipMapper.selectByItemId(itemId);
    }

    @Override
    public void deleteProductSipByItemId(Long itemId) {
        productSipMapper.deleteByItemId(itemId);
    }

}
