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

    default MailAccountDO selectByUserName(String userName){
        QueryWrapperX queryWrapperX = new QueryWrapperX<MailAccountDO>();
        queryWrapperX.eqIfPresent("username", userName);
        return this.selectOne(queryWrapperX);
    };
}
