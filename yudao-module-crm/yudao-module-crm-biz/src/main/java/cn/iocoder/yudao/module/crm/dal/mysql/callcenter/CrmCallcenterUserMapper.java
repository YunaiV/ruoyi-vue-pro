package cn.iocoder.yudao.module.crm.dal.mysql.callcenter;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.business.CrmBusinessPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.callcenter.vo.CrmCallcenterUserPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.callcenter.vo.CrmCallcenterUserRespVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.callcenter.CrmCallcenterUserDO;
import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.util.CrmPermissionUtils;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.RoleDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;

/**
 * 呼叫中心用户绑定 Mapper
 *
 * @author fhqsuhpv
 */
@Mapper
public interface CrmCallcenterUserMapper extends BaseMapperX<CrmCallcenterUserDO> {

    default PageResult<CrmCallcenterUserDO> selectPage(CrmCallcenterUserPageReqVO reqVO) {
        return selectPage(reqVO, new MPJLambdaWrapperX<CrmCallcenterUserDO>()
                .eqIfPresent(CrmCallcenterUserDO::getUserId, reqVO.getUserId())
                .eqIfPresent(CrmCallcenterUserDO::getYunkeCallcenterUserId, reqVO.getYunkeCallcenterUserId())
                .eqIfPresent(CrmCallcenterUserDO::getYunkeCallcenterPhone, reqVO.getYunkeCallcenterPhone())
                .eqIfPresent(CrmCallcenterUserDO::getLianlianCallcenterUserId, reqVO.getLianlianCallcenterUserId())
                .eqIfPresent(CrmCallcenterUserDO::getLianlianCallcenterPhone, reqVO.getLianlianCallcenterPhone())
                .betweenIfPresent(CrmCallcenterUserDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(CrmCallcenterUserDO::getId));
    }

}
