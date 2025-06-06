package com.somle.otto.service;

import com.somle.otto.repository.OttoAccountDao;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class OttoService {


    @Autowired
    @Qualifier("ottoAccountDao")
    OttoAccountDao accountDao;

    public List<OttoClient> ottoClients;

    @PostConstruct
    public void init() {
        ottoClients = accountDao.findAll().stream().map(ottoAccount -> {
                OttoClient ottoClient = new OttoClient(ottoAccount, accountDao);
                return ottoClient;
            }
        ).toList();
    }
}
