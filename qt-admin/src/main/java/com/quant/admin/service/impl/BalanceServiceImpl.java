package com.quant.admin.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.quant.admin.dao.BalanceMapper;
import com.quant.admin.entity.Balance;
import com.quant.admin.service.BalanceService;
import com.quant.common.enums.Status;
import com.quant.core.api.ApiResult;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author yang
 * @since 2019-04-15
 */
@Service
public class BalanceServiceImpl extends ServiceImpl<BalanceMapper, Balance> implements BalanceService {

    @Override
    public ApiResult getBalanceListByAccountId(String accountId) {
        try {
            Wrapper<Balance> balanceWrapper = new EntityWrapper<>();
            balanceWrapper.eq("account_id", accountId);
            balanceWrapper.orderDesc(Collections.singleton("trade_balance"));
            Balance balance = new Balance();
            List<Balance> balances = balance.selectList(balanceWrapper);
            return new ApiResult(Status.SUCCESS, balances);
        } catch (Exception e) {
            return new ApiResult(Status.ERROR);
        }
    }
}
