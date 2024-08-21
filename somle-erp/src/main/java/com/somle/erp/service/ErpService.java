package com.somle.erp.service;

import com.somle.erp.model.ErpCountrySku;
import com.somle.erp.model.ErpDepartment;
import com.somle.erp.model.ErpStyleSku;
import com.somle.erp.repository.ErpCountrySkuRepository;
import com.somle.erp.repository.ErpDepartmentRepository;
import com.somle.erp.repository.ErpStyleSkuRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Collections;
import java.util.stream.Stream;

@Slf4j
@Service
public class ErpService {

    @Autowired
    MessageChannel productChannel;

    @Autowired
    MessageChannel dataChannel;

    @Autowired
    ErpStyleSkuRepository styleSkuRepository;

    @Autowired
    ErpCountrySkuRepository countrySkuRepository;

    // @Autowired
    // EsbPlatformRepository esbPlatformRepository;

    @Autowired
    MessageChannel departmentChannel;

    @Autowired
    ErpDepartmentRepository departmentRepository;


    
    // @PostConstruct
    // public void init() {
    // }

    // @EventListener(ApplicationReadyEvent.class)
    // @Transactional(readOnly = true)
    // public void test() {
    //     EsbCountrySku x = countrySkuRepository.findAll().get(0);
    //     log.debug(x.getStyleSku().toString());
    // }


    public ErpDepartment getDepartment(Long id) {
        // return dingTalkService.toEsb(dingTalkService.getDepartment(Long.valueOf(id)));
        return departmentRepository.findById(id).get();
    }

    public ErpDepartment getDepartment(String nameZh) {
        return departmentRepository.findByNameZh(nameZh).get();
    }

    public Stream<ErpDepartment> getDepartmentParents(Long id) {
        if (id == null) {
            return Stream.of();
        } else {
            ErpDepartment current = getDepartment(id);
            return Stream.concat(Stream.of(current), getDepartmentParents(current.getParentId()));
        }
    }

    public ErpDepartment getParent(Long id, Integer parentLevel) {
        List<ErpDepartment> parents = getDepartmentParents(id).toList();
        return parents.get(parents.size() - 1 - parentLevel);
    }

    public List<ErpDepartment> getChildren(Long id) {
        return departmentRepository.findByParentId(id);
    }

    public ErpDepartment getEsbDepartmentTree(Long id) {
        var result = getDepartment(id);
        List<ErpDepartment> children = getChildren(id);
        if (children != null) {
            result.setChildren(children.stream().map(child->getEsbDepartmentTree(child.getId())).toList());
        }
        return result;
    }

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

        return styleSkuRepository.findAll(Example.of(styleSku), pageable);
    }




    public boolean addProduct(
        List<ErpCountrySku> productList
    ) {
        for (ErpCountrySku product : productList) {
            productChannel.send(MessageBuilder.withPayload(product).build());
        }
        return true;
    }

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

    // public List<EsbPlatform> getPlatforms() {
    //     return esbPlatformRepository.findAll();
    // }

    public boolean saveDepartment(
        ErpDepartment department
    ) {
        departmentRepository.save(department);
        departmentChannel.send(MessageBuilder.withPayload(department).build());
        return true;
    }
}
