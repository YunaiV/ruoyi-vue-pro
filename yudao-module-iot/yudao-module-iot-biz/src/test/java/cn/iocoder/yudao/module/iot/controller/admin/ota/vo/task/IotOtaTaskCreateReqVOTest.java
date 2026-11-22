package cn.iocoder.yudao.module.iot.controller.admin.ota.vo.task;

import cn.iocoder.yudao.module.iot.enums.ota.IotOtaTaskDeviceScopeEnum;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link IotOtaTaskCreateReqVO} 的参数校验单元测试
 *
 * @author 芋道源码
 */
public class IotOtaTaskCreateReqVOTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    public void testValidate_selectWithDeviceIds() {
        // 准备参数
        IotOtaTaskCreateReqVO reqVO = buildReqVO(IotOtaTaskDeviceScopeEnum.SELECT.getScope(), Arrays.asList(1L, 2L));

        // 调用，并断言
        assertTrue(validator.validate(reqVO).isEmpty());
    }

    @Test
    public void testValidate_selectWithoutDeviceIds() {
        for (List<Long> deviceIds : Arrays.asList(null, Collections.<Long>emptyList())) {
            // 准备参数
            IotOtaTaskCreateReqVO reqVO = buildReqVO(IotOtaTaskDeviceScopeEnum.SELECT.getScope(), deviceIds);

            // 调用
            Set<ConstraintViolation<IotOtaTaskCreateReqVO>> violations = validator.validate(reqVO);
            // 断言
            assertEquals(1, violations.size(), String.valueOf(deviceIds));
            assertEquals("deviceIdsValid", violations.iterator().next().getPropertyPath().toString());
        }
    }

    @Test
    public void testValidate_allWithoutDeviceIds() {
        // 准备参数：全部设备时，不需要选中设备
        IotOtaTaskCreateReqVO reqVO = buildReqVO(IotOtaTaskDeviceScopeEnum.ALL.getScope(), null);

        // 调用，并断言
        assertTrue(validator.validate(reqVO).isEmpty());
    }

    private static IotOtaTaskCreateReqVO buildReqVO(Integer deviceScope, List<Long> deviceIds) {
        IotOtaTaskCreateReqVO reqVO = new IotOtaTaskCreateReqVO();
        reqVO.setName("升级任务");
        reqVO.setFirmwareId(1024L);
        reqVO.setDeviceScope(deviceScope);
        reqVO.setDeviceIds(deviceIds);
        return reqVO;
    }

}
