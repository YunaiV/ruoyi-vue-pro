package cn.iocoder.yudao.module.oa.dal.mysql.officialdoc;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.oa.controller.admin.officialdoc.vo.template.OaOfficialDocTemplatePageReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.officialdoc.OaOfficialDocTemplateDO;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * OA 套红模板 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface OaOfficialDocTemplateMapper extends BaseMapperX<OaOfficialDocTemplateDO> {

    default PageResult<OaOfficialDocTemplateDO> selectPage(OaOfficialDocTemplatePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<OaOfficialDocTemplateDO>()
                .likeIfPresent(OaOfficialDocTemplateDO::getName, reqVO.getName())
                .eqIfPresent(OaOfficialDocTemplateDO::getStatus, reqVO.getStatus()).orderByAsc(OaOfficialDocTemplateDO::getSort).orderByDesc(OaOfficialDocTemplateDO::getId));
    }

    default List<OaOfficialDocTemplateDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<OaOfficialDocTemplateDO>()
                .eqIfPresent(OaOfficialDocTemplateDO::getStatus, status).orderByAsc(OaOfficialDocTemplateDO::getSort).orderByDesc(OaOfficialDocTemplateDO::getId));
    }

}
