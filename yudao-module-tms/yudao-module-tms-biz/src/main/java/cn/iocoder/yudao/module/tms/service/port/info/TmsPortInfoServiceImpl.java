package cn.iocoder.yudao.module.tms.service.port.info;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.tms.controller.admin.port.info.vo.TmsPortInfoPageReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.port.info.vo.TmsPortInfoSaveReqVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.port.info.TmsPortInfoDO;
import cn.iocoder.yudao.module.tms.dal.mysql.port.info.TmsPortInfoMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.tms.enums.TmsErrorCodeConstants.PORT_INFO_NAME_DUPLICATE;
import static cn.iocoder.yudao.module.tms.enums.TmsErrorCodeConstants.PORT_INFO_NOT_EXISTS;

/**
 * TMS港口信息 Service 实现类
 *
 * @author wdy
 */
@Service
@Validated
public class TmsPortInfoServiceImpl implements TmsPortInfoService {

    @Resource
    private TmsPortInfoMapper portInfoMapper;

    @Override
    public Long createPortInfo(TmsPortInfoSaveReqVO createReqVO) {
        // 校验港口名称重复
        validatePortInfoNameDuplicate(null, createReqVO.getName());
        // 插入
        TmsPortInfoDO portInfo = BeanUtils.toBean(createReqVO, TmsPortInfoDO.class);
        portInfoMapper.insert(portInfo);
        // 返回
        return portInfo.getId();
    }

    @Override
    public void updatePortInfo(TmsPortInfoSaveReqVO updateReqVO) {
        // 校验存在
        validatePortInfoExists(updateReqVO.getId());
        // 校验港口名称重复
        validatePortInfoNameDuplicate(updateReqVO.getId(), updateReqVO.getName());
        // 更新
        TmsPortInfoDO updateObj = BeanUtils.toBean(updateReqVO, TmsPortInfoDO.class);
        portInfoMapper.updateById(updateObj);
    }

    @Override
    public void deletePortInfo(Long id) {
        // 校验存在
        validatePortInfoExists(id);
        // 删除
        portInfoMapper.deleteById(id);
    }

    @Override
    public TmsPortInfoDO validatePortInfoExists(Long id) {
        TmsPortInfoDO portInfo = portInfoMapper.selectById(id);
        if (portInfo == null) {
            throw exception(PORT_INFO_NOT_EXISTS, id);
        }
        return portInfo;
    }

    private void validatePortInfoNameDuplicate(Long id, String name) {
        TmsPortInfoDO portInfo = portInfoMapper.selectOne(TmsPortInfoDO::getName, name);
        if (portInfo != null && !portInfo.getId().equals(id)) {
            throw exception(PORT_INFO_NAME_DUPLICATE, name);
        }
    }

    @Override
    public TmsPortInfoDO getPortInfo(Long id) {
        return portInfoMapper.selectById(id);
    }

    @Override
    public PageResult<TmsPortInfoDO> getPortInfoPage(TmsPortInfoPageReqVO pageReqVO) {
        return portInfoMapper.selectPage(pageReqVO);
    }

    @Override
    public List<TmsPortInfoDO> getPortInfoList() {
        return portInfoMapper.selectList();
    }

    @Override
    public List<TmsPortInfoDO> validatePortInfoExistsList(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<TmsPortInfoDO> portInfoList = portInfoMapper.selectByIds(ids);
        if (portInfoList.size() != ids.size()) {
            // 找出不存在的ID
            Set<Long> existIds = portInfoList.stream()
                .map(TmsPortInfoDO::getId)
                .collect(Collectors.toSet());
            Set<Long> notExistIds = ids.stream()
                .filter(id -> !existIds.contains(id))
                .collect(Collectors.toSet());
            throw exception(PORT_INFO_NOT_EXISTS, notExistIds);
        }
        return portInfoList;
    }
}