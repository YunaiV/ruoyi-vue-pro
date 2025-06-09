package cn.iocoder.yudao.module.tms.convert.first.mile.request;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.tms.controller.admin.first.mile.request.item.vo.TmsFirstMileRequestItemSaveReqVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.first.mile.request.TmsFirstMileRequestDO;
import cn.iocoder.yudao.module.tms.dal.dataobject.first.mile.request.item.TmsFirstMileRequestItemDO;
import cn.iocoder.yudao.module.tms.service.bo.TmsFirstMileRequestBO;
import cn.iocoder.yudao.module.tms.service.bo.TmsFirstMileRequestItemItemBO;

import java.util.List;

/**
 * 头程单申请 Convert
 *
 * @author wdy
 */
public class TmsFirstMileRequestConvert {

    /**
     * 将 DO 和明细列表转换为 BO
     *
     * @param firstMileRequest DO
     * @param itemList         明细列表
     * @return BO
     */
    public static TmsFirstMileRequestBO convert(TmsFirstMileRequestDO firstMileRequest, List<TmsFirstMileRequestItemDO> itemList) {
        return BeanUtils.toBean(firstMileRequest, TmsFirstMileRequestBO.class, bo -> bo.setItems(itemList));
    }

    /**
     * 将明细 VO 列表转换为 DO 列表
     *
     * @param itemList 明细 VO 列表
     * @return 明细 DO 列表
     */
    public static List<TmsFirstMileRequestItemDO> convertItemList(List<TmsFirstMileRequestItemSaveReqVO> itemList) {
        return BeanUtils.toBean(itemList, TmsFirstMileRequestItemDO.class);
    }



    /**
     * 将明细 BO 转换为 DO
     *
     * @param itemBO 明细 BO
     * @return 明细 DO
     */
    public static TmsFirstMileRequestItemDO convertItem(TmsFirstMileRequestItemItemBO itemBO) {
        return BeanUtils.toBean(itemBO, TmsFirstMileRequestItemDO.class);
    }

}