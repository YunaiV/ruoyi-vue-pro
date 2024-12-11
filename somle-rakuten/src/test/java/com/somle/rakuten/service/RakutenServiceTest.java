package com.somle.rakuten.service;


import com.somle.framework.test.core.ut.BaseDbUnitTest;
import com.somle.rakuten.model.polo.RakutenTokenEntity;
import com.somle.rakuten.model.vo.OrderRequestVO;
import com.somle.rakuten.repository.RakutenTokenRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Import({RakutenService.class})
class RakutenServiceTest extends BaseDbUnitTest {

    @Resource
    RakutenService service;
    @Resource
    RakutenTokenRepository repository;

    @Test
    void getRakuten() {
        ArrayList<String> list = new ArrayList<>();
        list.add("372286-20240601-0197014591");
        OrderRequestVO vo = new OrderRequestVO();
        vo.setVersion(8);
        vo.setOrderNumberList(list);

        Object object = service.client.getOrders(vo);
        System.out.println(object);
    }
    @Test
    void getRakutenToken() {
        List<RakutenTokenEntity> all = repository.findAll();
        System.out.println("all = " + all);
    }

}