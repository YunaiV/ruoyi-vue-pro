package com.somle.walmart.service;


import com.somle.walmart.model.WalmartShipNode;
import com.somle.walmart.model.WalmartToken;
import com.somle.walmart.repository.WalmartShipNodeRepository;
import com.somle.walmart.repository.WalmartTokenRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
public class WalmartService {

    public List<WalmartClient> walmartClients = new ArrayList<>();
    @Resource
    private WalmartTokenRepository tokenRepository;
    @Resource
    private WalmartShipNodeRepository shipNodeRepository;

    @PostConstruct
    public void init() {
        List<WalmartToken> tokens = tokenRepository.findAll();
        for (WalmartToken token : tokens) {
            if (token.getSvcName().equals("Walmart Marketplace")) {
                walmartClients.add(new WalmartMarketplaceClient(token));
            } else {
                List<WalmartShipNode> walmartShipNodeList = shipNodeRepository.findAll();
                List<String> shipNodes = walmartShipNodeList.stream()
                    .filter(walmartShipNode -> walmartShipNode.getClientId().equals(token.getClientId()))
                    .map(WalmartShipNode::getShipNode)
                    .collect(Collectors.toList());
                shipNodes.forEach(shipNode -> {
                    walmartClients.add(new WalmartDsvClient(token, shipNode));
                });
            }
        }
    }
}