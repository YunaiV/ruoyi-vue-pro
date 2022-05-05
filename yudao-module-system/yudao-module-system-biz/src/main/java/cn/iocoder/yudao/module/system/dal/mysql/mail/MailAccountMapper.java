package cn.iocoder.yudao.module.system.dal.mysql.mail;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.account.MailAccountPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailAccountDO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailTemplateDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Date;

@Mapper
public interface MailAccountMapper extends BaseMapperX<MailAccountDO> {

    default PageResult<MailAccountDO> selectPage(MailAccountPageReqVO pageReqVO) {
        return selectPage(pageReqVO, new QueryWrapperX<MailAccountDO>()
                .likeIfPresent("from_address" , pageReqVO.getFromAddress())
                .likeIfPresent("host" , pageReqVO.getHost())
                .likeIfPresent("username" , pageReqVO.getUsername())
                .eqIfPresent("password" , pageReqVO.getPassword())
                .eqIfPresent("port" , pageReqVO.getPort())
        );
    }

    default MailAccountDO selectByUserName(String userName){
        return selectOne(new QueryWrapperX<MailAccountDO>()
                .eqIfPresent("username" , userName));
    }

    default MailAccountDO selectByUserNameAndId(String userName,Long id){
        return selectOne(new QueryWrapperX<MailAccountDO>()
                .eqIfPresent("username" , userName)
                .neIfPresent("id" , id));
    }

    default MailAccountDO selectOneByFrom(String from){
        return selectOne(new QueryWrapperX<MailAccountDO>()
                .eqIfPresent("from_address" , from));
    }

    @Select("SELECT COUNT(*) FROM system_mail_account WHERE update_time > #{maxUpdateTime}")
    Long selectCountByUpdateTimeGt(Date maxUpdateTime);
}
