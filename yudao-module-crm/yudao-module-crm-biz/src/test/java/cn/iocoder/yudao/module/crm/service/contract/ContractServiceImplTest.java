package cn.iocoder.yudao.module.crm.service.contract;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.crm.controller.admin.contract.vo.CrmContractCreateReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.contract.vo.CrmContractPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.contract.vo.CrmContractUpdateReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contract.CrmContractDO;
import cn.iocoder.yudao.module.crm.dal.mysql.contract.CrmContractMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.CONTRACT_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link CrmContractServiceImpl} 的单元测试类
 *
 * @author dhb52
 */
@Import(CrmContractServiceImpl.class)
public class ContractServiceImplTest extends BaseDbUnitTest {

    @Resource
    private CrmContractServiceImpl contractService;

    @Resource
    private CrmContractMapper contractMapper;

    @Test
    public void testCreateContract_success() {
        // 准备参数
        CrmContractCreateReqVO reqVO = randomPojo(CrmContractCreateReqVO.class);

        // 调用
        Long contractId = contractService.createContract(reqVO, getLoginUserId());
        // 断言
        assertNotNull(contractId);
        // 校验记录的属性是否正确
        CrmContractDO contract = contractMapper.selectById(contractId);
        assertPojoEquals(reqVO, contract);
    }

    @Test
    public void testUpdateContract_success() {
        // mock 数据
        CrmContractDO dbContract = randomPojo(CrmContractDO.class);
        contractMapper.insert(dbContract);// @Sql: 先插入出一条存在的数据
        // 准备参数
        CrmContractUpdateReqVO reqVO = randomPojo(CrmContractUpdateReqVO.class, o -> {
            o.setId(dbContract.getId()); // 设置更新的 ID
        });

        // 调用
        contractService.updateContract(reqVO);
        // 校验是否更新正确
        CrmContractDO contract = contractMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, contract);
    }

    @Test
    public void testUpdateContract_notExists() {
        // 准备参数
        CrmContractUpdateReqVO reqVO = randomPojo(CrmContractUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> contractService.updateContract(reqVO), CONTRACT_NOT_EXISTS);
    }

    @Test
    public void testDeleteContract_success() {
        // mock 数据
        CrmContractDO dbContract = randomPojo(CrmContractDO.class);
        contractMapper.insert(dbContract);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbContract.getId();

        // 调用
        contractService.deleteContract(id);
        // 校验数据不存在了
        assertNull(contractMapper.selectById(id));
    }

    @Test
    public void testDeleteContract_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> contractService.deleteContract(id), CONTRACT_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetContractPage() {
        // mock 数据
        CrmContractDO dbContract = randomPojo(CrmContractDO.class, o -> { // 等会查询到
            o.setName(null);
            o.setCustomerId(null);
            o.setBusinessId(null);
            o.setOrderDate(null);
            o.setNo(null);
            o.setDiscountPercent(null);
            o.setProductPrice(null);
        });
        contractMapper.insert(dbContract);
        // 测试 name 不匹配
        contractMapper.insert(cloneIgnoreId(dbContract, o -> o.setName(null)));
        // 测试 customerId 不匹配
        contractMapper.insert(cloneIgnoreId(dbContract, o -> o.setCustomerId(null)));
        // 测试 no 不匹配
        contractMapper.insert(cloneIgnoreId(dbContract, o -> o.setNo(null)));
        // 准备参数
        CrmContractPageReqVO reqVO = new CrmContractPageReqVO();
        reqVO.setName(null);
        reqVO.setCustomerId(null);
        reqVO.setBusinessId(null);
        reqVO.setNo(null);

        // 调用
        PageResult<CrmContractDO> pageResult = contractService.getContractPage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbContract, pageResult.getList().get(0));
    }

}
