package cn.iocoder.yudao.module.report.service.ureport;

import cn.hutool.core.collection.CollectionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.report.controller.admin.ureport.vo.UreportFilePageReqVO;
import cn.iocoder.yudao.module.report.controller.admin.ureport.vo.UreportFileSaveReqVO;
import cn.iocoder.yudao.module.report.dal.dataobject.ureport.UreportFileDO;
import cn.iocoder.yudao.module.report.dal.mysql.ureport.UreportFileMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.report.enums.ErrorCodeConstants.UREPORT_FILE_NOT_EXISTS;

/**
 * Ureport2报表 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class UreportFileServiceImpl implements UreportFileService {

    @Resource
    private UreportFileMapper ureportFileMapper;

    @Override
    public Long createUreportFile(UreportFileSaveReqVO createReqVO) {
        // 插入
        UreportFileDO ureportFile = BeanUtils.toBean(createReqVO, UreportFileDO.class);
        ureportFileMapper.insert(ureportFile);
        // 返回
        return ureportFile.getId();
    }

    @Override
    public void updateUreportFile(UreportFileSaveReqVO updateReqVO) {
        // 校验存在
        validateUreportFileExists(updateReqVO.getId());
        // 更新
        UreportFileDO updateObj = BeanUtils.toBean(updateReqVO, UreportFileDO.class);
        ureportFileMapper.updateById(updateObj);
    }

    @Override
    public void deleteUreportFile(Long id) {
        // 校验存在
        validateUreportFileExists(id);
        // 删除
        ureportFileMapper.deleteById(id);
    }

    private void validateUreportFileExists(Long id) {
        if (ureportFileMapper.selectById(id) == null) {
            throw exception(UREPORT_FILE_NOT_EXISTS);
        }
    }

    @Override
    public UreportFileDO getUreportFile(Long id) {
        return ureportFileMapper.selectById(id);
    }

    @Override
    public PageResult<UreportFileDO> getUreportFilePage(UreportFilePageReqVO pageReqVO) {
        return ureportFileMapper.selectPage(pageReqVO);
    }

    @Override
    public Long checkExistByName(String name) {
        // TODO @赤焰：Service 处理业务逻辑，不能出现 Mapper 层的东西，收敛成 mapper 的一个操作方法
        LambdaQueryWrapper<UreportFileDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UreportFileDO::getFileName,name);
        return ureportFileMapper.selectCount(queryWrapper);
    }

    @Override
    public UreportFileDO queryUreportFileDoByName(String name) {
        // TODO @赤焰：Service 处理业务逻辑，不能出现 Mapper 层的东西，收敛成 mapper 的一个操作方法
        LambdaQueryWrapper<UreportFileDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UreportFileDO::getFileName,name);
        // TODO @赤焰：这里，可以用 selectByName 即可
        List<UreportFileDO> list = ureportFileMapper.selectList(queryWrapper);
        if(CollectionUtil.isNotEmpty(list)){
            return list.get(0);
        }
        return null;
    }

    @Override
    public List<UreportFileDO> queryReportFileList() {
        // TODO @赤焰：Service 处理业务逻辑，不能出现 Mapper 层的东西，收敛成 mapper 的一个操作方法
        LambdaQueryWrapper<UreportFileDO> queryWrapper = new LambdaQueryWrapper<>();
        return ureportFileMapper.selectList(queryWrapper);
    }

    @Override
    public int deleteReportFileByName(String name) {
        // TODO @赤焰：Service 处理业务逻辑，不能出现 Mapper 层的东西，收敛成 mapper 的一个操作方法
        LambdaQueryWrapper<UreportFileDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UreportFileDO::getFileName,name);
        return ureportFileMapper.delete(queryWrapper);
    }

}
