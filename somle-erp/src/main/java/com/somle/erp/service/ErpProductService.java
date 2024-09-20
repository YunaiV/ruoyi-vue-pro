package com.somle.erp.service;

import com.somle.erp.model.ErpDepartment;
import com.somle.erp.model.product.ErpCountrySku;
import com.somle.erp.model.product.ErpStyleSku;
import com.somle.erp.repository.ErpCountrySkuRepository;
import com.somle.erp.repository.ErpDepartmentRepository;
import com.somle.erp.repository.ErpStyleSkuRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
public class ErpProductService {

//    @Autowired
//    MessageChannel productChannel;
//
//    @Autowired
//    MessageChannel dataChannel;
//
//    @Autowired
//    MessageChannel departmentChannel;

    @Autowired
    ErpStyleSkuRepository styleSkuRepository;

    @Autowired
    ErpCountrySkuRepository countrySkuRepository;




    public Page<ErpStyleSku> getStyleSku(ErpStyleSku styleSku, Pageable pageable) {
        // Specification<EsbStyleSku> spec = (root, query, criteriaBuilder) -> {
        //     List<Predicate> predicates = new ArrayList<>();

        //     if (name != null && !name.isEmpty()) {
        //         predicates.add(criteriaBuilder.equal(root.get("name"), name));
        //     }

        //     if (price != null) {
        //         predicates.add(criteriaBuilder.greaterThan(root.get("price"), price));
        //     }

        //     return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        // };

        ExampleMatcher matcher = ExampleMatcher.matching()
            .withIgnoreCase() // case-insensitive
            .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING); // partial match

        return styleSkuRepository.findAll(Example.of(styleSku, matcher), pageable);
    }




//    public boolean syncProduct(
//        List<ErpCountrySku> productList
//    ) {
//        for (ErpCountrySku product : productList) {
//            productChannel.send(MessageBuilder.withPayload(product).build());
//        }
//        return true;
//    }

    // @Transactional
    public boolean saveProduct(
        ErpCountrySku countrySku
    ) {
        ErpStyleSku styleSku = countrySku.getStyleSku();
        styleSku = styleSkuRepository.findByStyleSku(styleSku.getStyleSku()).orElse(styleSku);
        styleSkuRepository.save(styleSku);
        countrySkuRepository.save(countrySku);
        // productChannel.send(MessageBuilder.withPayload(product).build());
        return true;
    }

    public boolean saveStyleSku(
        ErpStyleSku styleSku
    ) {
        log.debug("saving style sku");
        styleSkuRepository.findByStyleSku(styleSku.getStyleSku())
            .map(existingEntity -> {
                // Copy properties from newEntity to existingEntity
                BeanUtils.copyProperties(styleSku, existingEntity);
                return styleSkuRepository.save(existingEntity);
            })
            .orElseGet(() -> {
                // Save new entity
                return styleSkuRepository.save(styleSku);
            });
        return true;
    }

}
