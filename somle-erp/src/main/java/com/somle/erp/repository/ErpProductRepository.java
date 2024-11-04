package com.somle.erp.repository;


import com.somle.erp.model.ErpProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @className: ErpProductRepository
 * @author: Wqh
 * @date: 2024/10/28 15:04
 * @Version: 1.0
 * @description:
 */
@Repository
public interface ErpProductRepository extends JpaRepository<ErpProduct, Long> {
    @Query(value = "SELECT\n" +
            "\ta.id AS rule_id,\n" +
            "CASE\n" +
            "\t\t\n" +
            "\t\tWHEN a.country_code = 'CN' THEN\n" +
            "\t\tCONCAT( b.`code`, \"-\", 'CHN' ) \n" +
            "\t\tWHEN a.country_code = 'US' THEN\n" +
            "\t\tCONCAT( b.`code`, \"-\", 'USA' ) \n" +
            "\t\tWHEN a.country_code = 'IN' THEN\n" +
            "\t\tCONCAT( b.`code`, \"-\", 'IND' ) \n" +
            "\t\tWHEN a.country_code = 'UK' THEN\n" +
            "\t\tCONCAT( b.`code`, \"-\", 'EU' ) \n" +
            "\t\tWHEN a.country_code = 'SA' THEN\n" +
            "\t\tCONCAT( b.`code`, \"-\", 'KSA' ) ELSE CONCAT( b.`code`, \"-\", a.country_code ) \n" +
            "\tEND AS product_sku,\n" +
            "\tc.`name` AS product_title,\n" +
            "\tc.image_url,\n" +
            "\tb.package_weight AS pd_net_weight,\n" +
            "\tb.package_length AS pd_net_length,\n" +
            "\tb.package_width AS pd_net_width,\n" +
            "\tb.package_height AS pd_net_height,\n" +
            "\tc.width AS product_weight,\n" +
            "\tc.length AS product_length,\n" +
            "\tc.width AS product_width,\n" +
            "\tc.height AS product_height,\n" +
            "\tc.material AS product_material,\n" +
            "\tc.purchase_price AS product_purchase_value,\n" +
            "\tb.purchase_price_currency_code AS currency_code,\n" +
            "\ta.logistic_attribute AS logistic_attribute,\n" +
            "\ta.hscode AS hs_code,\n" +
            "\ta.declared_value AS product_declared_value,\n" +
            "\ta.declared_value_currency_code AS pd_declare_currency_code,\n" +
            "\ta.declared_type AS pd_oversea_type_cn,\n" +
            "\ta.declared_type_en AS pd_oversea_type_en,\n" +
            "\ta.tax_rate AS fbo_tax_rate,\n" +
            "\ta.country_code AS country_code,\n" +
            "\te.`name` AS user_organization_id,\n" +
            "\tc.bar_code,\n" +
            "\tf.`name` AS product_dept_name,\n" +
            "\tc.dept_id AS product_dept_id \n" +
            "FROM\n" +
            "\terp_custom_rule a\n" +
            "\tLEFT JOIN ( SELECT * FROM erp_supplier_product WHERE deleted = 0 ) b ON a.supplier_product_id = b.id\n" +
            "\tLEFT JOIN ( SELECT * FROM erp_product WHERE deleted = 0 ) c ON b.product_id = c.id\n" +
            "\tLEFT JOIN ( SELECT * FROM system_users WHERE deleted = 0 ) d ON c.creator = d.id\n" +
            "\tLEFT JOIN ( SELECT * FROM system_dept WHERE deleted = 0 ) e ON d.dept_id = e.id\n" +
            "\tLEFT JOIN ( SELECT * FROM system_dept WHERE deleted = 0 ) f ON c.dept_id = f.id \n" +
            "WHERE\n" +
            "\ta.deleted = 0", nativeQuery = true)
    List<ErpProduct> findAllProducts();
}
