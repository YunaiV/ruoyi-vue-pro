package com.somle.otto;

import com.somle.framework.test.core.ut.BaseDbUnitTest;
import com.somle.otto.model.pojo.OttoAccount;
import com.somle.otto.repository.OttoAccountDao;
import com.somle.otto.repository.OttoAuthTokenDao;
import com.somle.otto.service.OttoService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

@Slf4j
@Import({OttoService.class, OttoAccountDao.class, OttoAuthTokenDao.class})
public class DemoTest  extends BaseDbUnitTest {
    @Resource
    OttoAccountDao ottoAccountDao;
    @Resource
    OttoAuthTokenDao ottoAuthTokenDao;
    @Resource
    OttoService service;

    @Test
    void getAll() {
        for (OttoAccount token : ottoAccountDao.findAll()) {
            System.out.println("token = " + token);
        }

    }
}
