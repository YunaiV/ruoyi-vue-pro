package com.somle.esb.service;

import com.dingtalk.api.response.OapiV2UserGetResponse;
import com.somle.dingtalk.model.DingTalkDepartment;
import com.somle.esb.model.EsbMapping;
import com.somle.esb.repository.EsbMappingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EsbMappingService {

    @Autowired
    private EsbMappingRepository mappingRepository;

    public EsbMapping toMapping(DingTalkDepartment dingTalkDepartment) {
        var mapping = new EsbMapping()
            .setType("department")
            .setDomain("dingtalk")
            .setExternalId(dingTalkDepartment.getDeptId().toString());
        return mapping;
    }

    public EsbMapping toMapping(OapiV2UserGetResponse.UserGetResponse dingTalkUser) {
        var mapping = new EsbMapping()
            .setType("user")
            .setDomain("dingtalk")
            .setExternalId(dingTalkUser.getUserid());
        return mapping;
    }

    public EsbMapping findMapping(EsbMapping mapping) {
        return mappingRepository.findOne(Example.of(mapping)).get();
    }

    public void save(EsbMapping mapping) {
        mappingRepository.save(mapping);
    }
}
