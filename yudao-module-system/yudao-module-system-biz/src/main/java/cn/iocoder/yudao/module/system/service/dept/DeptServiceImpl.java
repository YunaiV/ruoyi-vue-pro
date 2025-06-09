package cn.iocoder.yudao.module.system.service.dept;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.datapermission.core.annotation.DataPermission;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptLevelRespDTO;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptReqDTO;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptSaveReqDTO;
import cn.iocoder.yudao.module.system.controller.admin.dept.vo.dept.DeptListReqVO;
import cn.iocoder.yudao.module.system.controller.admin.dept.vo.dept.DeptTreeRespVO;
import cn.iocoder.yudao.module.system.convert.dept.DeptConvert;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO;
import cn.iocoder.yudao.module.system.dal.mysql.dept.DeptMapper;
import cn.iocoder.yudao.module.system.dal.redis.RedisKeyConstants;
import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.*;

/**
 * 部门 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class DeptServiceImpl implements DeptService {

    @Resource
    private MessageChannel departmentOutputChannel;

    @Resource
    private DeptMapper deptMapper;

    private DeptConvert deptConvert = DeptConvert.INSTANCE;

    @Override
    @CacheEvict(cacheNames = RedisKeyConstants.DEPT_CHILDREN_ID_LIST,
            allEntries = true) // allEntries 清空所有缓存，因为操作一个部门，涉及到多个缓存
    public Long createDept(DeptSaveReqDTO saveReqDTO) {
        if (saveReqDTO.getParentId() == null) {
            saveReqDTO.setParentId(DeptDO.PARENT_ID_ROOT);
        }
        // 校验父部门的有效性
        validateParentDept(null, saveReqDTO.getParentId());
        // 校验部门名的唯一性
        validateDeptNameUnique(null, saveReqDTO.getParentId(), saveReqDTO.getName());

        // 插入部门
        DeptDO deptDO = BeanUtils.toBean(saveReqDTO, DeptDO.class);
        deptMapper.insert(deptDO);

        // 发送管道
        departmentOutputChannel.send(MessageBuilder.withPayload(deptConvert.toRespDTO(deptDO)).build());
        return deptDO.getId();
    }

    @Override
    @CacheEvict(cacheNames = RedisKeyConstants.DEPT_CHILDREN_ID_LIST,
            allEntries = true) // allEntries 清空所有缓存，因为操作一个部门，涉及到多个缓存
    public void updateDept(DeptSaveReqDTO updateReqVO) {
        if (updateReqVO.getParentId() == null) {
            updateReqVO.setParentId(DeptDO.PARENT_ID_ROOT);
        }
        // 校验自己存在
        validateDeptExists(updateReqVO.getId());
        // 校验父部门的有效性
        validateParentDept(updateReqVO.getId(), updateReqVO.getParentId());
        // 校验部门名的唯一性
        validateDeptNameUnique(updateReqVO.getId(), updateReqVO.getParentId(), updateReqVO.getName());

        // 更新部门
        DeptDO updateObj = BeanUtils.toBean(updateReqVO, DeptDO.class);
        deptMapper.updateById(updateObj);

        // 发送管道
        departmentOutputChannel.send(MessageBuilder.withPayload(deptConvert.toRespDTO(updateObj)).build());
    }

//    @Override
//    @CacheEvict(cacheNames = RedisKeyConstants.DEPT_CHILDREN_ID_LIST,
//            allEntries = true) // allEntries 清空所有缓存，因为操作一个部门，涉及到多个缓存
//    public void deleteDept(Long id) {
//        // 校验是否存在
//        validateDeptExists(id);
//        // 校验是否有子部门
//        if (deptMapper.selectCountByParentId(id) > 0) {
//            throw exception(DEPT_EXITS_CHILDREN);
//        }
//        // 删除部门
//        deptMapper.deleteById(id);
//    }

    @Override
    @CacheEvict(cacheNames = RedisKeyConstants.DEPT_CHILDREN_ID_LIST,
        allEntries = true) // allEntries 清空所有缓存，因为操作一个部门，涉及到多个缓存
    public void deleteDept(Long id) {
        // 校验是否存在
        validateDeptExists(id);
        for (var dept : getChildDeptList(id)) {
            // 删除部门
            deptMapper.deleteById(dept.getId());
        }
        deptMapper.deleteById(id);
    }

    @VisibleForTesting
    void validateDeptExists(Long id) {
        if (id == null) {
            return;
        }
        DeptDO dept = deptMapper.selectById(id);
        if (dept == null) {
            throw exception(DEPT_NOT_FOUND);
        }
    }

    @VisibleForTesting
    void validateParentDept(Long id, Long parentId) {
        if (parentId == null || DeptDO.PARENT_ID_ROOT.equals(parentId)) {
            return;
        }
        // 1. 不能设置自己为父部门
        if (Objects.equals(id, parentId)) {
            throw exception(DEPT_PARENT_ERROR);
        }
        // 2. 父部门不存在
        DeptDO parentDept = deptMapper.selectById(parentId);
        if (parentDept == null) {
            throw exception(DEPT_PARENT_NOT_EXITS);
        }
        // 3. 递归校验父部门，如果父部门是自己的子部门，则报错，避免形成环路
        if (id == null) { // id 为空，说明新增，不需要考虑环路
            return;
        }
        for (int i = 0; i < Short.MAX_VALUE; i++) {
            // 3.1 校验环路
            parentId = parentDept.getParentId();
            if (Objects.equals(id, parentId)) {
                throw exception(DEPT_PARENT_IS_CHILD);
            }
            // 3.2 继续递归下一级父部门
            if (parentId == null || DeptDO.PARENT_ID_ROOT.equals(parentId)) {
                break;
            }
            parentDept = deptMapper.selectById(parentId);
            if (parentDept == null) {
                break;
            }
        }
    }

    @VisibleForTesting
    void validateDeptNameUnique(Long id, Long parentId, String name) {
        DeptDO dept = deptMapper.selectByParentIdAndName(parentId, name);
        if (dept == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的部门
        if (id == null) {
            throw exception(DEPT_NAME_DUPLICATE);
        }
        if (ObjectUtil.notEqual(dept.getId(), id)) {
            throw exception(DEPT_NAME_DUPLICATE);
        }
    }

    @Override
    public DeptDO getDept(Long id) {
        return deptMapper.selectById(id);
    }

    @Override
    public List<DeptRespDTO> listDepts(DeptReqDTO reqDTO) {
        return deptConvert.toRespDTOs(deptMapper.selectList(reqDTO));
    }

    @Override
    public List<DeptDO> getDeptList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return deptMapper.selectBatchIds(ids);
    }

    @Override
    public List<DeptDO> getChildDeptList(Collection<Long> ids) {
        List<DeptDO> children = new LinkedList<>();
        // 遍历每一层
        Collection<Long> parentIds = ids;
        // 使用 Short.MAX_VALUE 避免 bug 场景下，存在死循环
        for (int i = 0; i < Short.MAX_VALUE; i++) {
            // 查询当前层，所有的子部门
            List<DeptDO> depts = deptMapper.selectListByParentId(parentIds);
            // 1. 如果没有子部门，则结束遍历
            if (CollUtil.isEmpty(depts)) {
                break;
            }
            // 2. 如果有子部门，继续遍历
            children.addAll(depts);
            parentIds = convertSet(depts, DeptDO::getId);
        }
        return children;
    }

    @Override
    public List<DeptDO> getDeptListByLeaderUserId(Long id) {
        return deptMapper.selectListByLeaderUserId(id);
    }

    @Override
    @DataPermission(enable = false) // 禁用数据权限，避免建立不正确的缓存
    @Cacheable(cacheNames = RedisKeyConstants.DEPT_CHILDREN_ID_LIST, key = "#id")
    public Set<Long> getChildDeptIdListFromCache(Long id) {
        List<DeptDO> children = getChildDeptList(id);
        return convertSet(children, DeptDO::getId);
    }

    @Override
    public void validateDeptList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 获得科室信息
        Map<Long, DeptDO> deptMap = getDeptMap(ids);
        // 校验
        ids.forEach(id -> {
            DeptDO dept = deptMap.get(id);
            if (dept == null) {
                throw exception(DEPT_NOT_FOUND);
            }
            if (!CommonStatusEnum.ENABLE.getStatus().equals(dept.getStatus())) {
                throw exception(DEPT_NOT_ENABLE, dept.getName());
            }
        });
    }

    @Override
    public Integer getDeptLevel(Long id) {
        // 校验自己存在
        validateDeptExists(id);
        return getParentList(new TreeSet<>(),id, 0).size();
    }

    @Override
    public TreeSet<DeptLevelRespDTO> getDeptTreeLevel(Long id) {
        // 校验自己存在
        validateDeptExists(id);
        return getParentList(new TreeSet<>(),id, 0);
    }

    @Override
    public List<DeptTreeRespVO> getTreeDeptList() {
        //获取所有部门
        List<DeptDO> deptDOList = deptMapper.selectList();
        //过滤出二级部门
        List<DeptDO> secondLevelDept = deptDOList.stream().filter(deptDO -> getDeptLevel(deptDO.getId()) == 2).toList();
        List<DeptTreeRespVO> deptTreeRespVos = deptConvert.buildDeptTree(secondLevelDept);
        for (DeptTreeRespVO deptTreeRespVO : deptTreeRespVos){
            //获取所有子部门
            List<DeptDO> childDeptList = getChildDeptList(deptTreeRespVO.getId());
            List<DeptTreeRespVO> childDeptListTree = deptConvert.buildDeptTree(childDeptList);
            deptTreeRespVO.setChildren(childDeptListTree);
        }
        return deptTreeRespVos;
    }

    @Override
    public List<DeptDO> getDeptListByNames(Collection<String> names) {
        if(CollectionUtils.isEmpty(names)) {
            return List.of();
        }
        return deptMapper.selectListByNames(names);
    }

    private TreeSet<DeptLevelRespDTO> getParentList(TreeSet<DeptLevelRespDTO> deptList, Long id, Integer level){
        DeptDO deptDO = deptMapper.selectById(id);
        DeptLevelRespDTO dto = new DeptLevelRespDTO(deptDO.getId(),deptDO.getName(),level++);
        deptList.add(dto);
        //判断是否是顶级部门
        if (DeptDO.PARENT_ID_ROOT.equals(deptDO.getParentId())){
            return deptList;
        }
        //根据父id获取
        return getParentList(deptList,deptDO.getParentId(),level);
    }
}
