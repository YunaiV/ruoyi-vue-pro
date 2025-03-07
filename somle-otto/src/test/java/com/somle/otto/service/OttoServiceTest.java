package com.somle.otto.service;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.framework.test.core.ut.SomleBaseDbUnitTest;
import com.somle.otto.model.pojo.OttoAccount;
import com.somle.otto.repository.OttoAccountDao;
import com.somle.otto.repository.OttoAuthTokenDao;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;


@Slf4j
@Import({OttoService.class, OttoAccountDao.class, OttoAuthTokenDao.class})
class OttoServiceTest extends SomleBaseDbUnitTest {
    @Resource
    OttoAccountDao ottoAccountDao;
    @Resource
    OttoAuthTokenDao ottoAuthTokenDao;
    @Resource
    OttoService service;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    @Transactional
//    @Rollback(false)
    void save() {
        OttoAccount account = OttoAccount.builder().clientId("4b65ae80-eafb-4198-ab52-74a7c4953850")
                .clientSecret("5052e86a-5383-49a5-b483-4708e02eb1a3")
                .build();
        OttoAccount save = ottoAccountDao.save(account);
        System.out.println("save = " + save);
    }

    @Test
    void getAll() {
        for (OttoAccount token : ottoAccountDao.findAll()) {
            System.out.println("token = " + token);
        }

    }
}