package cn.iocoder.yudao.module.product.service.spu;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.*;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.SpuDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.product.convert.spu.SpuConvert;
import cn.iocoder.yudao.module.product.dal.mysql.spu.SpuMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.*;

/**
 * 商品spu Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class SpuServiceImpl implements SpuService {

    @Resource
    private SpuMapper spuMapper;

    @Override
    public Integer createSpu(SpuCreateReqVO createReqVO) {
        // 插入
        SpuDO spu = SpuConvert.INSTANCE.convert(createReqVO);
        spuMapper.insert(spu);
        // 返回
        return spu.getId();
    }

    @Override
    public void updateSpu(SpuUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateSpuExists(updateReqVO.getId());
        // 更新
        SpuDO updateObj = SpuConvert.INSTANCE.convert(updateReqVO);
        spuMapper.updateById(updateObj);
    }

    @Override
    public void deleteSpu(Integer id) {
        // 校验存在
        this.validateSpuExists(id);
        // 删除
        spuMapper.deleteById(id);
    }

    private void validateSpuExists(Integer id) {
        if (spuMapper.selectById(id) == null) {
            throw exception(SPU_NOT_EXISTS);
        }
    }

    @Override
    public SpuDO getSpu(Integer id) {
        return spuMapper.selectById(id);
    }

    @Override
    public List<SpuDO> getSpuList(Collection<Integer> ids) {
        return spuMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<SpuDO> getSpuPage(SpuPageReqVO pageReqVO) {
        return spuMapper.selectPage(pageReqVO);
    }

    @Override
    public List<SpuDO> getSpuList(SpuExportReqVO exportReqVO) {
        return spuMapper.selectList(exportReqVO);
    }

}
