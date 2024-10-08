package com.somle.erp.service;

import com.somle.erp.model.ErpDepartment;
import com.somle.erp.repository.ErpDepartmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
public class ErpDepartmentService {

//    @Autowired
//    MessageChannel productChannel;
//
//    @Autowired
//    MessageChannel dataChannel;
//
//    @Autowired
//    MessageChannel departmentChannel;

    // @Autowired
    // EsbPlatformRepository esbPlatformRepository;



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

    //get parents in ascending order, parent id 1 is exclusded
    public Stream<ErpDepartment> getDepartmentParents(Long id) {
        if (id == null || id == 1l) {
            return Stream.of();
        } else {
            ErpDepartment current = getDepartment(id);
            return Stream.concat(Stream.of(current), getDepartmentParents(current.getParentId()));
        }
    }

    public ErpDepartment getParent(Long id, Integer parentLevel) {
        List<ErpDepartment> parents = getDepartmentParents(id).toList();
        return parents.get(parents.size() - parentLevel);
    }


    public ErpDepartment getParent(ErpDepartment depart, Integer parentLevel) {
//        if (parentLevel <= 1) {
//            throw new RuntimeException(("parent level " + parentLevel + " is smaller then minimum level 1"));
//        }
//        if (parentLevel >= depart.getLevel()) {
//            throw new RuntimeException("parent level " + parentLevel + "should be lower than current level " + depart.getLevel());
//        }
//        var parent = departmentRepository.findByParentId(depart.getParentId()).getFirst();
//        return parent.getLevel() == parentLevel ? parent : getParent(parent, parentLevel)
        return getParent(depart.getId(), parentLevel);
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

    // public List<EsbPlatform> getPlatforms() {
    //     return esbPlatformRepository.findAll();
    // }

    public boolean saveDepartment(
        ErpDepartment department
    ) {
        departmentRepository.save(department);
//        departmentChannel.send(MessageBuilder.withPayload(department).build());
        return true;
    }
}
