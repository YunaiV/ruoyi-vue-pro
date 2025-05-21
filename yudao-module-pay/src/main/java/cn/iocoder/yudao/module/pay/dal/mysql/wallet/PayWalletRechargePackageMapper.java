package cn.iocoder.yudao.module.pay.dal.mysql.wallet;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.pay.controller.admin.wallet.vo.rechargepackage.WalletRechargePackagePageReqVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletRechargePackageDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PayWalletRechargePackageMapper extends BaseMapperX<PayWalletRechargePackageDO> {

    default PageResult<PayWalletRechargePackageDO> selectPage(WalletRechargePackagePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PayWalletRechargePackageDO>()
                .likeIfPresent(PayWalletRechargePackageDO::getName, reqVO.getName())
                .eqIfPresent(PayWalletRechargePackageDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(PayWalletRechargePackageDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(PayWalletRechargePackageDO::getPayPrice));
    }

    default PayWalletRechargePackageDO selectByName(String name) {
        return selectOne(PayWalletRechargePackageDO::getName, name);
    }

    default List<PayWalletRechargePackageDO> selectListByStatus(Integer status) {
        return selectList(PayWalletRechargePackageDO::getStatus, status);
    }

}
