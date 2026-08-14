package cn.iocoder.yudao.module.fms.controller.admin.voucher.vo;

import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAuxiliaryItemDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAuxiliaryTypeDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsSubjectDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsVoucherWordDO;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * FMS 凭证导入模板 VO
 *
 * @author 芋道源码
 */
@Data
@AllArgsConstructor
public class FmsVoucherImportTemplateVO {

    /**
     * 凭证字数组
     */
    private List<FmsVoucherWordDO> voucherWords;
    /**
     * 科目数组
     */
    private List<FmsSubjectDO> subjects;
    /**
     * 辅助核算类别数组
     */
    private List<FmsAuxiliaryTypeDO> auxiliaryTypes;
    /**
     * 辅助核算项目数组
     */
    private List<FmsAuxiliaryItemDO> auxiliaryItems;

}
