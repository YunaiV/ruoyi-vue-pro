package cn.iocoder.yudao.module.oa.dal.mysql.mail;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.oa.dal.dataobject.mail.OaMailAccountDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * OA 企业邮箱账号 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface OaMailAccountMapper extends BaseMapperX<OaMailAccountDO> {

    default List<OaMailAccountDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<OaMailAccountDO>()
                .select(OaMailAccountDO::getMail, OaMailAccountDO::getCreator).eq(OaMailAccountDO::getStatus, status)
                .orderByAsc(OaMailAccountDO::getMail).orderByAsc(OaMailAccountDO::getId));
    }

    default OaMailAccountDO selectByIdForUpdate(Long id) {
        return selectOneForUpdate(OaMailAccountDO::getId, id);
    }

    default List<OaMailAccountDO> selectListByCreatorAndStatus(String creator, Integer status) {
        return selectList(new LambdaQueryWrapperX<OaMailAccountDO>().eq(OaMailAccountDO::getCreator, creator)
                .eqIfPresent(OaMailAccountDO::getStatus, status)
                .orderByDesc(OaMailAccountDO::getDefaultStatus).orderByAsc(OaMailAccountDO::getId));
    }

    default List<OaMailAccountDO> selectListByCreatorForUpdate(String creator) {
        return selectList(new LambdaQueryWrapperX<OaMailAccountDO>().eq(OaMailAccountDO::getCreator, creator)
                .orderByAsc(OaMailAccountDO::getId).last("FOR UPDATE"));
    }

    default Long selectCountByProviderId(Long providerId) {
        return selectCount(OaMailAccountDO::getProviderId, providerId);
    }

}
