package com.somle.erp.service;

import com.somle.framework.test.core.ut.BaseSpringTest;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Import(ErpDepartmentService.class)
class ErpDepartmentServiceTest extends BaseSpringTest {

    @Resource
    ErpDepartmentService service;

    @Test
    void getParent() {
        var parent1 = service.getParent(697743722l, 2);
        assertEquals(697743722l, parent1.getId());

        var parent2 = service.getParent(697743722l, 1);
        assertEquals(53111133l, parent2.getId());

//        var parent3 = service.getParent(697743722l, 0);
//        assertEquals(1l, parent3.getId());
    }
}