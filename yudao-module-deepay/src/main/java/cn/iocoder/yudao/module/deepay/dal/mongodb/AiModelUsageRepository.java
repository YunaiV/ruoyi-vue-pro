package cn.iocoder.yudao.module.deepay.dal.mongodb;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * 模型用量 Repository。
 */
public interface AiModelUsageRepository extends MongoRepository<AiModelUsageDocument, String> {

    Page<AiModelUsageDocument> findByTenantIdOrderByCreatedAtDesc(Long tenantId, Pageable pageable);

    Page<AiModelUsageDocument> findByTenantIdAndCustomerIdOrderByCreatedAtDesc(
            Long tenantId, Long customerId, Pageable pageable);

    long countByTenantIdAndModel(Long tenantId, String model);

}
