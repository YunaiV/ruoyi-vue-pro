package cn.iocoder.yudao.module.oa.service.seal;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oa.controller.admin.seal.vo.*;
import cn.iocoder.yudao.module.oa.dal.dataobject.seal.OaSealDO;
import cn.iocoder.yudao.module.oa.dal.mysql.seal.OaSealMapper;
import cn.iocoder.yudao.module.oa.dal.redis.no.OaNoRedisDAO;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.*;
import static java.util.Collections.singleton;

/**
 * 印章 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class OaSealServiceImpl implements OaSealService {

    @Resource
    private OaNoRedisDAO noRedisDAO;

    @Resource
    private OaSealMapper sealMapper;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;

    @Resource
    @Lazy // 延迟，避免循环依赖报错
    private OaSealApplyService sealApplyService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSeal(OaSealSaveReqVO createReqVO) {
        // 1.1 校验保管人有效
        adminUserApi.validateUser(createReqVO.getKeeperUserId());
        // 1.2 校验保管部门有效
        deptApi.validateDeptList(singleton(createReqVO.getKeeperDeptId()));
        // 1.3 校验所属部门有效，允许与保管部门不同
        deptApi.validateDeptList(singleton(createReqVO.getDeptId()));
        // 1.4 生成印章编号，并校验唯一性
        String no = noRedisDAO.generate(OaNoRedisDAO.SEAL_NO_PREFIX);
        validateSealNoUnique(no);

        // 2. 新增印章，状态及日期按表单保存
        OaSealDO seal = BeanUtils.toBean(createReqVO, OaSealDO.class).setNo(no).setId(null);
        sealMapper.insert(seal);
        return seal.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSeal(OaSealSaveReqVO updateReqVO) {
        // 1.1 校验印章存在
        validateSealExists(updateReqVO.getId());
        // 1.2 校验保管人有效
        adminUserApi.validateUser(updateReqVO.getKeeperUserId());
        // 1.3 校验保管部门有效
        deptApi.validateDeptList(singleton(updateReqVO.getKeeperDeptId()));
        // 1.4 校验所属部门有效，允许与保管部门不同
        deptApi.validateDeptList(singleton(updateReqVO.getDeptId()));

        // 2. 更新印章及保管信息
        OaSealDO updateObj = BeanUtils.toBean(updateReqVO, OaSealDO.class);
        sealMapper.updateById(updateObj);
    }

    @Override
    public void deleteSeal(Long id) {
        // 1.1 校验印章存在
        validateSealExists(id);
        // 1.2 校验印章未被业务单据引用
        if (sealApplyService.getSealApplyCountBySealId(id) > 0) {
            throw exception(SEAL_IN_USE);
        }

        // 2. 逻辑删除台账
        sealMapper.deleteById(id);
    }

    @Override
    public PageResult<OaSealDO> getSealPage(OaSealPageReqVO pageReqVO) {
        return sealMapper.selectPage(pageReqVO);
    }

    @Override
    public OaSealDO validateSealExists(Long id) {
        OaSealDO seal = sealMapper.selectById(id);
        if (seal == null) {
            throw exception(SEAL_NOT_EXISTS);
        }
        return seal;
    }

    /**
     * 校验印章编号唯一
     *
     * @param no 业务编号
     */
    private void validateSealNoUnique(String no) {
        OaSealDO seal = sealMapper.selectByNo(no);
        if (seal != null) {
            throw exception(SEAL_NO_DUPLICATE);
        }
    }

}
