package com.quant.admin.task;

import com.quant.common.config.RedisUtil;
import com.quant.common.config.VpnProxyConfig;
import com.quant.common.constans.RobotRedisKeyConfig;
import com.quant.core.api.ApiClient;
import com.quant.common.response.Symbol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SymbolTask {

    private static Logger logger = LoggerFactory.getLogger(SymbolTask.class);
    @Autowired
    VpnProxyConfig vpnProxyConfig;
    @Autowired
    RedisUtil redisUtil;

    /**
     * 收集交易对信息存到数据库 /v1/common/symbols
     */
    @Async
    @Scheduled(fixedDelay = 1000 * 60 * 60 * 24, initialDelay = 5000)
    public void symbolCollects() {
        logger.info("=========数据 redis 同步交易对信息开始=========");
        ApiClient apiClient = new ApiClient(vpnProxyConfig);
        List<Symbol> symbols = apiClient.getSymbols();
        //获取到交易对 进行排序操作 防止每次都插入不一致
        symbols = symbols.stream().sorted(Comparator.comparing(Symbol::getSymbol)).collect(Collectors.toList());
        int i = 0;
        for (Symbol s : symbols) {
            ++i;
            com.quant.admin.entity.Symbol symbol = new com.quant.admin.entity.Symbol();
            symbol.setId(i);
            symbol.setAmountPrecision(s.getAmountPrecision());
            symbol.setBaseCurrency(s.getBaseCurrency());
            symbol.setPricePrecision(s.getPricePrecision());
            symbol.setQuoteCurrency(s.getQuoteCurrency());
            symbol.setSymbol(s.getSymbol());
            if (symbol.insertOrUpdate()) {
                //将交易对的quote保存redis
                redisUtil.set(RobotRedisKeyConfig.getSymbol() + symbol.getSymbol(), symbol.getBaseCurrency() + "_" + symbol.getQuoteCurrency() + "_" + symbol.getPricePrecision() + "_" + symbol.getAmountPrecision());
            }
        }

        logger.info("=========同步交易对信息完毕=========");
    }
}
