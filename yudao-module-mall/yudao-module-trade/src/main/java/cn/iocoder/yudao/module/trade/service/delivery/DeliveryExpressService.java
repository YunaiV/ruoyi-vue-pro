package cn.iocoder.yudao.module.trade.service.delivery;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.express.DeliveryExpressCreateReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.express.DeliveryExpressExportReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.express.DeliveryExpressPageReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.express.DeliveryExpressUpdateReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryExpressDO;

import javax.validation.Valid;
import java.util.List;

/**
 * 快递公司 Service 接口
 *
 * @author jason
 */
public interface DeliveryExpressService {

    /**
     * 创建快递公司
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createDeliveryExpress(@Valid DeliveryExpressCreateReqVO createReqVO);

    /**
     * 更新快递公司
     *
     * @param updateReqVO 更新信息
     */
    void updateDeliveryExpress(@Valid DeliveryExpressUpdateReqVO updateReqVO);

    /**
     * 删除快递公司
     *
     * @param id 编号
     */
    void deleteDeliveryExpress(Long id);

    /**
     * 获得快递公司
     *
     * @param id 编号
     * @return 快递公司
     */
    DeliveryExpressDO getDeliveryExpress(Long id);

    /**
     * 校验快递公司是否合法
     *
     * @param id 编号
     * @return 快递公司
     */
    DeliveryExpressDO validateDeliveryExpress(Long id);

    /**
     * 获得快递公司分页
     *
     * @param pageReqVO 分页查询
     * @return 快递公司分页
     */
    PageResult<DeliveryExpressDO> getDeliveryExpressPage(DeliveryExpressPageReqVO pageReqVO);

    /**
     * 获得快递公司列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 快递公司列表
     */
    List<DeliveryExpressDO> getDeliveryExpressList(DeliveryExpressExportReqVO exportReqVO);

    /**
     * 获取指定状态的快递公司列表
     *
     * @param status 状态
     * @return 快递公司列表
     */
    List<DeliveryExpressDO> getDeliveryExpressListByStatus(Integer status);

}
