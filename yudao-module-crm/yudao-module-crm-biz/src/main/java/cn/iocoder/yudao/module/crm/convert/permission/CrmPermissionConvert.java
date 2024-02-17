package cn.iocoder.yudao.module.crm.convert.permission;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.crm.controller.admin.permission.vo.CrmPermissionRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.permission.vo.CrmPermissionUpdateReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.permission.CrmPermissionDO;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.dept.dto.PostRespDTO;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.google.common.collect.Multimaps;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.MapUtils.findAndThen;

/**
 * Crm 数据权限 Convert
 *
 * @author HUIHUI
 */
@Mapper
public interface CrmPermissionConvert {

    CrmPermissionConvert INSTANCE = Mappers.getMapper(CrmPermissionConvert.class);

    default List<CrmPermissionRespVO> convert(List<CrmPermissionDO> permissions, List<AdminUserRespDTO> userList,
                                              Map<Long, DeptRespDTO> deptMap, Map<Long, PostRespDTO> postMap) {
        Map<Long, AdminUserRespDTO> userMap = CollectionUtils.convertMap(userList, AdminUserRespDTO::getId);
        return CollectionUtils.convertList(BeanUtils.toBean(permissions, CrmPermissionRespVO.class), item -> {
            findAndThen(userMap, item.getUserId(), user -> {
                item.setNickname(user.getNickname());
                findAndThen(deptMap, user.getDeptId(), deptRespDTO -> item.setDeptName(deptRespDTO.getName()));
                if (CollUtil.isEmpty(user.getPostIds())) {
                    item.setPostNames(Collections.emptySet());
                    return;
                }
                List<PostRespDTO> postList = MapUtils.getList(Multimaps.forMap(postMap), user.getPostIds());
                item.setPostNames(CollectionUtils.convertSet(postList, PostRespDTO::getName));
            });
            return item;
        });
    }

    default List<CrmPermissionDO> convertList(CrmPermissionUpdateReqVO updateReqVO) {
        return CollectionUtils.convertList(updateReqVO.getIds(),
                id -> new CrmPermissionDO().setId(id).setLevel(updateReqVO.getLevel()));
    }

}
