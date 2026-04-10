package cn.iocoder.yudao.module.mes.service.md.item;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.md.item.vo.sip.MesMdProductSipPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.item.vo.sip.MesMdProductSipSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdProductSipDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * MES 产品SIP Service 接口
 *
 * @author 芋道源码
 */
public interface MesMdProductSipService {

    /**
     * 创建产品SIP
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProductSip(@Valid MesMdProductSipSaveReqVO createReqVO);

    /**
     * 更新产品SIP
     *
     * @param updateReqVO 更新信息
     */
    void updateProductSip(@Valid MesMdProductSipSaveReqVO updateReqVO);

    /**
     * 删除产品SIP
     *
     * @param id 编号
     */
    void deleteProductSip(Long id);

    /**
     * 获得产品SIP
     *
     * @param id 编号
     * @return 产品SIP
     */
    MesMdProductSipDO getProductSip(Long id);

    /**
     * 获得产品SIP分页
     *
     * @param pageReqVO 分页查询
     * @return 产品SIP分页
     */
    PageResult<MesMdProductSipDO> getProductSipPage(MesMdProductSipPageReqVO pageReqVO);

    /**
     * 根据物料产品编号，获得产品SIP列表
     *
     * @param itemId 物料产品编号
     * @return 产品SIP列表
     */
    List<MesMdProductSipDO> getProductSipListByItemId(Long itemId);

    /**
     * 根据物料产品编号，删除产品SIP
     *
     * @param itemId 物料产品编号
     */
    void deleteProductSipByItemId(Long itemId);

}
