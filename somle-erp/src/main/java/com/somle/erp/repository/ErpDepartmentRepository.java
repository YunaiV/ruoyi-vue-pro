package com.somle.erp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.somle.erp.model.ErpDepartment;

public interface ErpDepartmentRepository extends JpaRepository<ErpDepartment, Long> {
    public List<ErpDepartment> findByParentId(Long id);
    public Optional<ErpDepartment> findByNameZh(String nameZh);
}