package cn.iocoder.yudao.module.hrm.service.employee.info;

import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.file.HrmEmployeeFileSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeFileDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.employee.info.HrmEmployeeFileMapper;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

/**
 * {@link HrmEmployeeFileServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(HrmEmployeeFileServiceImpl.class)
public class HrmEmployeeFileServiceImplTest extends BaseDbUnitTest {

    @Resource
    private HrmEmployeeFileServiceImpl employeeFileService;

    @Resource
    private HrmEmployeeFileMapper employeeFileMapper;

    @MockBean
    private HrmEmployeeService employeeService;

    @Test
    public void testSaveEmployeeFiles_diffByUrl() {
        // mock 数据
        Long employeeId = randomLongId();
        Integer fileType = 21;
        when(employeeService.validateEmployeeExists(employeeId)).thenReturn(
                randomPojo(HrmEmployeeDO.class, o -> o.setId(employeeId)));
        HrmEmployeeFileDO retainedFile = createEmployeeFile(employeeId, fileType, "https://example.com/keep.pdf");
        employeeFileMapper.insert(retainedFile);
        HrmEmployeeFileDO duplicateFile = createEmployeeFile(employeeId, fileType, retainedFile.getUrl());
        employeeFileMapper.insert(duplicateFile);
        HrmEmployeeFileDO deletedFile = createEmployeeFile(employeeId, fileType, "https://example.com/delete.pdf");
        employeeFileMapper.insert(deletedFile);
        // 准备参数
        HrmEmployeeFileSaveReqVO reqVO = new HrmEmployeeFileSaveReqVO().setEmployeeId(employeeId).setType(fileType)
                .setFileUrls(Arrays.asList(retainedFile.getUrl(), "https://example.com/create.pdf",
                        retainedFile.getUrl()));

        // 调用
        employeeFileService.saveEmployeeFiles(reqVO);

        // 断言
        List<HrmEmployeeFileDO> dbFiles = employeeFileMapper.selectListByEmployeeIdAndType(employeeId, fileType);
        assertEquals(2, dbFiles.size());
        Map<String, HrmEmployeeFileDO> dbFileMap = convertMap(dbFiles, HrmEmployeeFileDO::getUrl);
        assertEquals(retainedFile.getId(), dbFileMap.get(retainedFile.getUrl()).getId());
        assertEquals("https://example.com/create.pdf", dbFileMap.get("https://example.com/create.pdf").getUrl());
        assertNull(employeeFileMapper.selectById(duplicateFile.getId()));
        assertNull(employeeFileMapper.selectById(deletedFile.getId()));
    }

    private static HrmEmployeeFileDO createEmployeeFile(Long employeeId, Integer fileType, String fileUrl) {
        return new HrmEmployeeFileDO().setEmployeeId(employeeId).setType(fileType).setUrl(fileUrl);
    }

}
