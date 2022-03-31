package cn.iocoder.yudao.module.system.dal.mysql.mail;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.account.MailAccountPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailAccountDO;
import org.apache.ibatis.annotations.Mapper;

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
        // TODO @wangjingyi：selectOne 有封装的方法；然后，编码一定要学会使用泛型呀。例如说 QueryWrapperX<MailAccountDO> queryWrapperX = new QueryWrapperX<>();
        QueryWrapperX<MailAccountDO> queryWrapperX = new QueryWrapperX<>();
        queryWrapperX.eqIfPresent("username", userName);
        return this.selectOne(queryWrapperX);
    };
}
