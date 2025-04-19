package cn.iocoder.yudao.module.wms.dal.mysql.product;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.wms.controller.admin.pickup.vo.WmsPickupPageReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.pickup.WmsPickupDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.product.WmsProductDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 拣货单 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsProductMapper extends BaseMapperX<WmsProductDO> {

}
