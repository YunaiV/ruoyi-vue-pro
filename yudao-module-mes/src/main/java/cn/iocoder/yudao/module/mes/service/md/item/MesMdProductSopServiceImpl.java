package cn.iocoder.yudao.module.mes.service.md.item;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.md.item.vo.sop.MesMdProductSopPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.item.vo.sop.MesMdProductSopSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdProductSopDO;
import cn.iocoder.yudao.module.mes.dal.mysql.md.item.MesMdProductSopMapper;
import cn.iocoder.yudao.module.mes.service.pro.process.MesProProcessService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 产品SOP Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesMdProductSopServiceImpl implements MesMdProductSopService {

    @Resource
    private MesMdProductSopMapper productSopMapper;

    @Resource
    private MesProProcessService processService;

    @Override
    public Long createProductSop(MesMdProductSopSaveReqVO createReqVO) {
        // 校验数据
        validateProductSopSaveData(createReqVO, null);

        // 插入
        MesMdProductSopDO sop = BeanUtils.toBean(createReqVO, MesMdProductSopDO.class);
        productSopMapper.insert(sop);
        return sop.getId();
    }

    @Override
    public void updateProductSop(MesMdProductSopSaveReqVO updateReqVO) {
        // 校验存在
        validateProductSopExists(updateReqVO.getId());
        // 校验数据
        validateProductSopSaveData(updateReqVO, updateReqVO.getId());

        // 更新
        MesMdProductSopDO updateObj = BeanUtils.toBean(updateReqVO, MesMdProductSopDO.class);
        productSopMapper.updateById(updateObj);
    }

    private void validateProductSopSaveData(MesMdProductSopSaveReqVO reqVO, Long excludeId) {
        // 校验排列顺序的唯一性
        validateSortUnique(reqVO.getItemId(), reqVO.getSort(), excludeId);
        // 校验工序存在
        if (reqVO.getProcessId() != null) {
            processService.validateProcessExistsAndEnable(reqVO.getProcessId());
        }
    }

    @Override
    public void deleteProductSop(Long id) {
        // 校验存在
        validateProductSopExists(id);
        // 删除
        productSopMapper.deleteById(id);
    }

    private void validateProductSopExists(Long id) {
        if (productSopMapper.selectById(id) == null) {
            throw exception(MD_PRODUCT_SOP_NOT_EXISTS);
        }
    }

    private void validateSortUnique(Long itemId, Integer sort, Long excludeId) {
        Long count = productSopMapper.selectCountByItemIdAndSort(itemId, sort, excludeId);
        if (count > 0) {
            throw exception(MD_PRODUCT_SOP_SORT_DUPLICATE);
        }
    }

    @Override
    public MesMdProductSopDO getProductSop(Long id) {
        return productSopMapper.selectById(id);
    }

    @Override
    public PageResult<MesMdProductSopDO> getProductSopPage(MesMdProductSopPageReqVO pageReqVO) {
        return productSopMapper.selectPage(pageReqVO);
    }

    @Override
    public List<MesMdProductSopDO> getProductSopListByItemId(Long itemId) {
        return productSopMapper.selectByItemId(itemId);
    }

    @Override
    public void deleteProductSopByItemId(Long itemId) {
        productSopMapper.deleteByItemId(itemId);
    }

}
