package cn.iocoder.yudao.module.mp.convert.material;

import cn.iocoder.yudao.module.mp.controller.admin.material.vo.MpMaterialUploadRespVO;
import cn.iocoder.yudao.module.mp.dal.dataobject.account.MpAccountDO;
import cn.iocoder.yudao.module.mp.dal.dataobject.material.MpMaterialDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper
public interface MpMaterialConvert {

    MpMaterialConvert INSTANCE = Mappers.getMapper(MpMaterialConvert.class);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(source = "account.id", target = "accountId"),
            @Mapping(source = "account.appId", target = "appId"),
    })
    MpMaterialDO convert(String mediaId, String type, String url, MpAccountDO account);

    MpMaterialUploadRespVO convert(MpMaterialDO bean);

}
