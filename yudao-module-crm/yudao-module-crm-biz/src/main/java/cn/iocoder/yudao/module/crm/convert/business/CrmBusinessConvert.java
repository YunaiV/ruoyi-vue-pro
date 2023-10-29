package cn.iocoder.yudao.module.crm.convert.business;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.*;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Set;

/**
 * 商机 Convert
 *
 * @author ljlleo
 */
@Mapper
public interface CrmBusinessConvert {

    CrmBusinessConvert INSTANCE = Mappers.getMapper(CrmBusinessConvert.class);

    CrmBusinessDO convert(CrmBusinessCreateReqVO bean);

    CrmBusinessDO convert(CrmBusinessUpdateReqVO bean);

    CrmBusinessRespVO convert(CrmBusinessDO bean);

    PageResult<CrmBusinessRespVO> convertPage(PageResult<CrmBusinessDO> page);

    List<CrmBusinessExcelVO> convertList02(List<CrmBusinessDO> list);

    default CrmBusinessDO convert(CrmBusinessDO business, CrmTransferBusinessReqVO reqVO, Long userId) {
        Set<Long> rwUserIds = business.getRwUserIds();
        rwUserIds.remove(userId);
        rwUserIds.add(reqVO.getOwnerUserId()); // 读写权限加入新的负责人
        // TODO @puhui999：对原负责人，加个类似的处理：移除、转化为团队成员（只读、读写）
        return new CrmBusinessDO().setId(business.getId()).setOwnerUserId(reqVO.getOwnerUserId()) // 设置新负责人
                .setRwUserIds(rwUserIds);
    }

}
