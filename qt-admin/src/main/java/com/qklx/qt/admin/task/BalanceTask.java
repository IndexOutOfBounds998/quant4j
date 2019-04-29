package com.qklx.qt.admin.task;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.qklx.qt.admin.entity.Account;
import com.qklx.qt.common.config.RedisUtil;
import com.qklx.qt.core.api.ApiClient;
import com.qklx.qt.core.response.Balance;
import com.qklx.qt.core.response.BalanceBean;
import com.qklx.qt.core.response.BalanceResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * 余额更新 redisUtil.set(balance.getCurrency() + "_" + balance.getAccountId() + "_amount", balance.getTradeBalance());
 */
@Slf4j
@Component
public class BalanceTask {

    @Autowired
    RedisUtil redisUtil;

    @Value(value = "${landen.ip}")
    private String ip;
    @Value(value = "${landen.port}")
    private int port;


    @Async
    @Scheduled(fixedDelay = 60000, initialDelay = 5000)
    public void updateBalance() {
        Account account = new Account();
        List<Account> accounts = account.selectAll();
        for (Account a : accounts) {
            ApiClient client = new ApiClient(a.getAccessKey(), a.getSecretKey(), ip, port);
            createBalance(client, String.valueOf(a.getId()));
        }
    }

    /**
     * 10s更新一次 防止错误
     * 更新redis账户余额
     */
    @Async
    @Scheduled(fixedDelay = 10000, initialDelay = 5000)
    public void updateRedisBalance() {
        Account account = new Account();
        List<Account> accounts = account.selectAll();
        for (Account a : accounts) {
            ApiClient client = new ApiClient(a.getAccessKey(), a.getSecretKey(), ip, port);
            BalanceResponse<Balance<List<BalanceBean>>> response = null;
            try {
                response = client.balance(String.valueOf(a.getId()));
            } catch (Exception e) {
                log.error("更新账户余额异常{}", e.getMessage());
            }
            if (response != null && response.getStatus().equals("ok")) {
                CopyOnWriteArrayList<BalanceBean> balances = new CopyOnWriteArrayList<>(response.getData().getList());
                if (!balances.isEmpty()) {
                    for (BalanceBean bean : balances) {
                        if (bean.getType().equals("trade")) {
                            redisUtil.set(bean.getCurrency() + "_balance_" + a.getId(), bean.getBalance());
                        }
                    }
                }
            }
        }
    }

    /**
     * 添加或者更新余额
     */
    private void createBalance(ApiClient client, String accountId) {
        BalanceResponse<Balance<List<BalanceBean>>> response = null;
        try {
            response = client.balance(accountId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (response == null) {
            return;
        }
        if (response.getStatus().equals("ok")) {
            CopyOnWriteArrayList<BalanceBean> balances = new CopyOnWriteArrayList<>(response.getData().getList());
            if (!balances.isEmpty()) {
                for (BalanceBean bean : balances) {
                    List<BalanceBean> temp = balances.stream().filter(balanceBean -> balanceBean.getCurrency().equals(bean.getCurrency())).collect(Collectors.toList());
                    com.qklx.qt.admin.entity.Balance balance = new com.qklx.qt.admin.entity.Balance();
                    balance.setAccountId(Integer.valueOf(accountId));
                    balance.setCurrency(bean.getCurrency());
                    for (BalanceBean b : temp) {
                        if (b.getType().equals("trade")) {
                            balance.setTradeBalance(b.getBalance());
                        } else {
                            balance.setFrozenBalance(b.getBalance());
                        }
                    }
                    balances.removeAll(temp);
                    if (balance.getFrozenBalance() != null && balance.getTradeBalance() != null) {
                        //查询是否已经插入过了
                        com.qklx.qt.admin.entity.Balance ckBalance = new com.qklx.qt.admin.entity.Balance();
                        Wrapper<com.qklx.qt.admin.entity.Balance> balanceWrapper = new EntityWrapper<>();
                        balanceWrapper.eq("account_id", accountId);
                        balanceWrapper.eq("currency", bean.getCurrency());
                        com.qklx.qt.admin.entity.Balance one = ckBalance.selectOne(balanceWrapper);
                        balance.setType("交易账户");
                        if (one != null) {
                            balance.setId(one.getId());
                            balance.updateById();
                        } else {
                            balance.insert();
                        }
                    }
                }
            }
        }

    }
}
