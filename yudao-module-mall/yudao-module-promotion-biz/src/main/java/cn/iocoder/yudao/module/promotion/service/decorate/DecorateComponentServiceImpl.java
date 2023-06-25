package cn.iocoder.yudao.module.promotion.service.decorate;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.promotion.controller.admin.decorate.vo.DecorateComponentSaveReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.decorate.vo.DecorateComponentSaveReqVO.ComponentReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.decorate.DecorateComponentDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.decorate.DecorateComponentMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.promotion.convert.decorate.DecorateComponentConvert.INSTANCE;

/**
 * 装修组件 Service 实现
 *
 * @author jason
 */
@Service
public class DecorateComponentServiceImpl implements DecorateComponentService {

    @Resource
    private DecorateComponentMapper decorateComponentMapper;

    @Override
    public void savePageComponents(DecorateComponentSaveReqVO reqVO) {
        // 1.新增或修改页面组件
        List<DecorateComponentDO> oldList = decorateComponentMapper.selectByPage(reqVO.getPageId());

        decorateComponentMapper.saveOrUpdateBatch(INSTANCE.convertList(reqVO.getPageId(), reqVO.getComponents()));
        // 2.删除相关组件
        Set<Long> deleteIds = convertSet(oldList, DecorateComponentDO::getId);
        deleteIds.removeAll(convertSet(reqVO.getComponents(), ComponentReqVO::getId, vo->Objects.nonNull(vo.getId())));
        if (CollUtil.isNotEmpty(deleteIds)) {
            decorateComponentMapper.deleteBatchIds(deleteIds);
        }
    }

    @Override
    public List<DecorateComponentDO> getPageComponents(Integer pageId) {
        return decorateComponentMapper.selectByPage(pageId);
    }

}
