package cn.iocoder.yudao.module.mes.service.md.item;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.md.item.vo.sop.MesMdProductSopPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.item.vo.sop.MesMdProductSopSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdProductSopDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * MES 产品SOP Service 接口
 *
 * @author 芋道源码
 */
public interface MesMdProductSopService {

    /**
     * 创建产品SOP
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProductSop(@Valid MesMdProductSopSaveReqVO createReqVO);

    /**
     * 更新产品SOP
     *
     * @param updateReqVO 更新信息
     */
    void updateProductSop(@Valid MesMdProductSopSaveReqVO updateReqVO);

    /**
     * 删除产品SOP
     *
     * @param id 编号
     */
    void deleteProductSop(Long id);

    /**
     * 获得产品SOP
     *
     * @param id 编号
     * @return 产品SOP
     */
    MesMdProductSopDO getProductSop(Long id);

    /**
     * 获得产品SOP分页
     *
     * @param pageReqVO 分页查询
     * @return 产品SOP分页
     */
    PageResult<MesMdProductSopDO> getProductSopPage(MesMdProductSopPageReqVO pageReqVO);

    /**
     * 根据物料产品编号，获得产品SOP列表
     *
     * @param itemId 物料产品编号
     * @return 产品SOP列表
     */
    List<MesMdProductSopDO> getProductSopListByItemId(Long itemId);

    /**
     * 根据物料产品编号，删除产品SOP
     *
     * @param itemId 物料产品编号
     */
    void deleteProductSopByItemId(Long itemId);

}
