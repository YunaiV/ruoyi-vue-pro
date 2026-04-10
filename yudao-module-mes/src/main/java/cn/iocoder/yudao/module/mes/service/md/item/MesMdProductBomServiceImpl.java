package cn.iocoder.yudao.module.mes.service.md.item;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.md.item.vo.bom.MesMdProductBomPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.item.vo.bom.MesMdProductBomSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdProductBomDO;
import cn.iocoder.yudao.module.mes.dal.mysql.md.item.MesMdProductBomMapper;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.*;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap2;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 产品BOM Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesMdProductBomServiceImpl implements MesMdProductBomService {

    @Resource
    private MesMdProductBomMapper productBomMapper;

    @Resource
    @Lazy // 避免循环依赖
    private MesMdItemService itemService;

    @Override
    public Long createProductBom(MesMdProductBomSaveReqVO createReqVO) {
        // 1.1 校验数据
        validateProductBomSaveData(createReqVO);
        // 1.2 校验不能形成闭环
        if (hasBomCycle(createReqVO.getItemId(), createReqVO.getBomItemId())) {
            throw exception(MD_PRODUCT_BOM_CIRCULAR);
        }

        // 2. 插入
        MesMdProductBomDO productBom = BeanUtils.toBean(createReqVO, MesMdProductBomDO.class);
        productBomMapper.insert(productBom);
        return productBom.getId();
    }

    @Override
    public void updateProductBom(MesMdProductBomSaveReqVO updateReqVO) {
        // 1.1 校验存在
        validateProductBomExists(updateReqVO.getId());
        // 1.2 校验数据
        validateProductBomSaveData(updateReqVO);

        // 更新
        MesMdProductBomDO updateObj = BeanUtils.toBean(updateReqVO, MesMdProductBomDO.class);
        productBomMapper.updateById(updateObj);
    }

    private void validateProductBomSaveData(MesMdProductBomSaveReqVO reqVO) {
        // 校验物料产品存在
        validateItemExists(reqVO.getItemId());
        // 校验BOM物料存在
        validateItemExists(reqVO.getBomItemId());
        // 校验不能自引用
        if (reqVO.getItemId().equals(reqVO.getBomItemId())) {
            throw exception(MD_PRODUCT_BOM_SELF_REFERENCE);
        }
    }

    @Override
    public void deleteProductBom(Long id) {
        // 校验存在
        validateProductBomExists(id);
        // 删除
        productBomMapper.deleteById(id);
    }

    private void validateProductBomExists(Long id) {
        if (productBomMapper.selectById(id) == null) {
            throw exception(MD_PRODUCT_BOM_NOT_EXISTS);
        }
    }

    private void validateItemExists(Long itemId) {
        if (itemService.getItem(itemId) == null) {
            throw exception(MD_ITEM_NOT_EXISTS);
        }
    }

    /**
     * 检测新增边 (itemId -> bomItemId) 后，BOM 图是否存在闭环
     *
     * @param itemId 父物料编号
     * @param bomItemId BOM子物料编号
     * @return 是否存在闭环
     */
    private boolean hasBomCycle(Long itemId, Long bomItemId) {
        // 1.1 获取所有已有的 BOM 记录
        List<MesMdProductBomDO> allBoms = productBomMapper.selectAll();
        // 1.2 构建邻接表：parent -> Set<child>
        Map<Long, Set<Long>> graph = convertMultiMap2(allBoms,
                MesMdProductBomDO::getItemId, MesMdProductBomDO::getBomItemId);
        // 1.3  添加待新增的边
        graph.computeIfAbsent(itemId, k -> new HashSet<>()).add(bomItemId);
        // 2. DFS 检测环
        for (Long node : graph.keySet()) {
            if (CollectionUtils.dfs(node, graph)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public MesMdProductBomDO getProductBom(Long id) {
        return productBomMapper.selectById(id);
    }

    @Override
    public PageResult<MesMdProductBomDO> getProductBomPage(MesMdProductBomPageReqVO pageReqVO) {
        return productBomMapper.selectPage(pageReqVO);
    }

    @Override
    public List<MesMdProductBomDO> getProductBomListByItemId(Long itemId) {
        return productBomMapper.selectByItemId(itemId);
    }

    @Override
    public List<MesMdProductBomDO> getProductBomListByItemIds(Collection<Long> itemIds) {
        return productBomMapper.selectByItemIds(itemIds);
    }

    @Override
    public void deleteProductBomByItemId(Long itemId) {
        productBomMapper.deleteByItemId(itemId);
    }

}
