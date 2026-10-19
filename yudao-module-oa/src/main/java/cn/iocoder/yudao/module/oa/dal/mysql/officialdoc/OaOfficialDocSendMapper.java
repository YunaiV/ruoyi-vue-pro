package cn.iocoder.yudao.module.oa.dal.mysql.officialdoc;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.oa.controller.admin.officialdoc.vo.send.OaOfficialDocSendPageReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.officialdoc.OaOfficialDocSendDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * OA 公文发文 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface OaOfficialDocSendMapper extends BaseMapperX<OaOfficialDocSendDO> {

    default Long selectCountByTemplateId(Long id) {
        return selectCount(OaOfficialDocSendDO::getTemplateId, id);
    }

    default OaOfficialDocSendDO selectByNo(String no) {
        return selectOne(OaOfficialDocSendDO::getNo, no);
    }

    default Long selectCountByDocumentNoAndIdNot(String documentNo, Long id) {
        return selectCount(new LambdaQueryWrapperX<OaOfficialDocSendDO>()
                .eq(OaOfficialDocSendDO::getDocumentNo, documentNo)
                .neIfPresent(OaOfficialDocSendDO::getId, id));
    }

    default PageResult<OaOfficialDocSendDO> selectPage(Long userId, OaOfficialDocSendPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<OaOfficialDocSendDO>()
                .eq(OaOfficialDocSendDO::getCreator, userId.toString())
                .likeIfPresent(OaOfficialDocSendDO::getTitle, reqVO.getTitle())
                .likeIfPresent(OaOfficialDocSendDO::getDocumentNo, reqVO.getDocumentNo())
                .eqIfPresent(OaOfficialDocSendDO::getStatus, reqVO.getStatus()).orderByDesc(OaOfficialDocSendDO::getId));
    }

}
