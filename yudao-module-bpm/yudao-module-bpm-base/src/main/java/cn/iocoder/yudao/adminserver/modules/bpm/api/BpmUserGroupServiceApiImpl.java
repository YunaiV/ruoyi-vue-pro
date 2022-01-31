package cn.iocoder.yudao.adminserver.modules.bpm.api;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.adminserver.modules.bpm.convert.definition.BpmUserGroupConvert;
import cn.iocoder.yudao.adminserver.modules.bpm.dal.dataobject.definition.BpmUserGroupDO;
import cn.iocoder.yudao.adminserver.modules.bpm.dal.mysql.definition.BpmUserGroupMapper;
import cn.iocoder.yudao.coreservice.modules.bpm.api.group.BpmUserGroupServiceApi;
import cn.iocoder.yudao.coreservice.modules.bpm.api.group.dto.BpmUserGroupDTO;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.adminserver.modules.bpm.enums.BpmErrorCodeConstants.USER_GROUP_IS_DISABLE;
import static cn.iocoder.yudao.adminserver.modules.bpm.enums.BpmErrorCodeConstants.USER_GROUP_NOT_EXISTS;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * Bpm 用户组 API 接口 实现类
 *
 * @author 芋道源码
 * @author jason
 */
@Service
public class BpmUserGroupServiceApiImpl implements BpmUserGroupServiceApi {

    @Resource
    private BpmUserGroupMapper userGroupMapper;

    @Override
    public List<BpmUserGroupDTO> getUserGroupList(Collection<Long> ids) {
        return BpmUserGroupConvert.INSTANCE.convertList3(userGroupMapper.selectBatchIds(ids));
    }


    @Override
    public void validUserGroups(Set<Long> ids) {

        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 获得用户组信息
        List<BpmUserGroupDO> userGroups = userGroupMapper.selectBatchIds(ids);
        Map<Long, BpmUserGroupDO> userGroupMap = CollectionUtils.convertMap(userGroups, BpmUserGroupDO::getId);
        // 校验
        ids.forEach(id -> {
            BpmUserGroupDO userGroup = userGroupMap.get(id);
            if (userGroup == null) {
                throw ServiceExceptionUtil.exception(USER_GROUP_NOT_EXISTS);
            }
            if (!CommonStatusEnum.ENABLE.getStatus().equals(userGroup.getStatus())) {
                throw exception(USER_GROUP_IS_DISABLE, userGroup.getName());
            }
        });
    }
}
