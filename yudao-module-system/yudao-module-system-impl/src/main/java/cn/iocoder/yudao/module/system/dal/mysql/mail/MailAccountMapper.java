package cn.iocoder.yudao.module.system.dal.mysql.mail;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.account.MailAccountPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailAccountDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

@Mapper
public interface MailAccountMapper extends BaseMapperX<MailAccountDO> {

    default PageResult<MailAccountDO> selectPage(MailAccountPageReqVO pageReqVO) {
        return selectPage(pageReqVO, new QueryWrapperX<MailAccountDO>()
                .likeIfPresent("form" , pageReqVO.getFrom())
                .likeIfPresent("host" , pageReqVO.getHost())
                .likeIfPresent("username" , pageReqVO.getUsername())
                .eqIfPresent("password" , pageReqVO.getPassword())
                .eqIfPresent("port" , pageReqVO.getPort())
        );
    }

    // TODO @wangjingyi：不要提供这样的泛的方法，而是明确的查询方法
    default MailAccountDO selectByParams(Map params){
        QueryWrapperX queryWrapperX = new QueryWrapperX<MailAccountDO>();
        params.forEach((k , v)->{
            queryWrapperX.eqIfPresent((String) k, v);
        });
        return this.selecOne(queryWrapperX);
    };
}
