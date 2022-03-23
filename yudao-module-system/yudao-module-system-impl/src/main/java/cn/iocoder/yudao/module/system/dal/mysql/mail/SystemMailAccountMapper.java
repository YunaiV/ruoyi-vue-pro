package cn.iocoder.yudao.module.system.dal.mysql.mail;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.SystemMailAccountDO;
import org.apache.ibatis.annotations.Mapper;

// TODO @ジョイイ： Mapper 一般不用注释，因为用途不大
/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author wangjingyi
 * @since 2022-03-21
 */
@Mapper
public interface SystemMailAccountMapper extends BaseMapperX<SystemMailAccountDO> {

    default PageResult<SystemMailAccountDO> selectPage(PageParam pageParam) {
        return selectPage(pageParam, new QueryWrapperX<SystemMailAccountDO>());
    }

}
