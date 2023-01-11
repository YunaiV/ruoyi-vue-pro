package cn.iocoder.yudao.module.mp.service.material;

import cn.iocoder.yudao.module.mp.controller.admin.material.vo.MpMaterialUploadTemporaryReqVO;
import cn.iocoder.yudao.module.mp.dal.dataobject.material.MpMaterialDO;
import me.chanjar.weixin.common.api.WxConsts;

import javax.validation.Valid;
import java.io.IOException;

/**
 * 公众号素材 Service 接口
 *
 * @author 芋道源码
 */
public interface MpMaterialService {

    /**
     * 获得素材的 URL
     *
     * 该 URL 来自我们自己的文件服务器存储的 URL，不是公众号存储的 URL
     *
     * @param accountId 公众号账号编号
     * @param mediaId 公众号素材 id
     * @param type 文件类型 {@link WxConsts.MediaFileType}
     * @return 素材的 URL
     */
    String downloadMaterialUrl(Long accountId, String mediaId, String type);

    MpMaterialDO uploadTemporaryMaterial(@Valid MpMaterialUploadTemporaryReqVO reqVO) throws IOException;
}
