package cn.iocoder.yudao.module.infra.service.file;

import cn.iocoder.yudao.adminserver.BaseDbUnitTest;
import cn.iocoder.yudao.framework.test.core.util.AssertUtils;
import cn.iocoder.yudao.module.infra.controller.admin.file.vo.InfFilePageReqVO;
import cn.iocoder.yudao.module.infra.service.file.impl.InfFileServiceImpl;
import cn.iocoder.yudao.coreservice.modules.infra.dal.dataobject.file.InfFileDO;
import cn.iocoder.yudao.coreservice.modules.infra.dal.mysql.file.InfFileCoreMapper;
import cn.iocoder.yudao.coreservice.modules.infra.framework.file.config.FileProperties;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.buildTime;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Import({InfFileServiceImpl.class, FileProperties.class})
public class InfFileServiceTest extends BaseDbUnitTest {

    @Resource
    private InfFileService fileService;

    @MockBean
    private FileProperties fileProperties;

    @Resource
    private InfFileCoreMapper fileMapper;

    @Test
    public void testGetFilePage() {
        // mock 数据
        InfFileDO dbFile = randomPojo(InfFileDO.class, o -> { // 等会查询到
            o.setId("yudao");
            o.setType("jpg");
            o.setCreateTime(buildTime(2021, 1, 15));
        });
        fileMapper.insert(dbFile);
        // 测试 id 不匹配
        fileMapper.insert(ObjectUtils.cloneIgnoreId(dbFile, o -> o.setId("tudou")));
        // 测试 type 不匹配
        fileMapper.insert(ObjectUtils.cloneIgnoreId(dbFile, o -> {
            o.setId("yudao02");
            o.setType("png");
        }));
        // 测试 createTime 不匹配
        fileMapper.insert(ObjectUtils.cloneIgnoreId(dbFile, o -> {
            o.setId("yudao03");
            o.setCreateTime(buildTime(2020, 1, 15));
        }));
        // 准备参数
        InfFilePageReqVO reqVO = new InfFilePageReqVO();
        reqVO.setId("yudao");
        reqVO.setType("jp");
        reqVO.setBeginCreateTime(buildTime(2021, 1, 10));
        reqVO.setEndCreateTime(buildTime(2021, 1, 20));

        // 调用
        PageResult<InfFileDO> pageResult = fileService.getFilePage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        AssertUtils.assertPojoEquals(dbFile, pageResult.getList().get(0), "content");
    }

}
