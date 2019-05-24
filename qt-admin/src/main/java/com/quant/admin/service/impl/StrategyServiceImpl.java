package com.quant.admin.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.quant.admin.dao.StrategyMapper;
import com.quant.admin.entity.Strategy;
import com.quant.admin.service.StrategyService;
import com.quant.core.api.ApiResult;
import com.quant.common.enums.Status;
import com.quant.common.vo.StrategyVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author yang
 * @since 2019-04-15
 */
@Slf4j
@Service
public class StrategyServiceImpl extends ServiceImpl<StrategyMapper, Strategy> implements StrategyService {

    @Override
    public ApiResult addOrUpdateStrategy(StrategyVo strategyVo, String uid) {
        if (strategyVo == null) {
            return new ApiResult(Status.ERROR);
        }
        Strategy strategy = new Strategy();
        StrategyVo.BaseInfoEntity baseInfo = strategyVo.getBaseInfo();
        if (baseInfo != null) {
            //基础信息配置i
            strategy.setUserId(Integer.valueOf(uid));
            strategy.setStrategyName(baseInfo.getStrategyName());
            strategy.setBuyAmount(baseInfo.getBuyAmount());
            strategy.setSellAmount(baseInfo.getSellAmount());
            strategy.setBuyPrice(baseInfo.getBuyPrice());
            strategy.setSellPrice(baseInfo.getSellPrice());
            strategy.setIsAllBuy(baseInfo.getIsAllBuy());
            strategy.setIsAllSell(baseInfo.getIsAllSell());
            strategy.setIsLimitPrice(baseInfo.getIsLimitPrice());
            strategy.setBuyAllWeights(baseInfo.getBuyAllWeights());
            strategy.setSellAllWeights(baseInfo.getSellAllWeights());
            strategy.setSleep(baseInfo.getSleep());
            strategy.setProfit(baseInfo.getProfit());
            strategy.setBuyQuotaPrice(baseInfo.getBuyQuotaPrice());
        }
        StrategyVo.Setting1Entity setting1 = strategyVo.getSetting1();
        if (setting1 != null) {

            strategy.setSetting1(JSON.toJSONString(setting1));

        }
        StrategyVo.Setting2Entity setting2 = strategyVo.getSetting2();
        if (setting2 != null) {

            strategy.setSetting2(JSON.toJSONString(setting2));
        }

        StrategyVo.Setting3Entity setting3 = strategyVo.getSetting3();
        if (setting3 != null) {
            strategy.setSetting3(JSON.toJSONString(setting3));
        }

        StrategyVo.Setting4Entity setting4 = strategyVo.getSetting4();
        if (setting4 != null) {
            strategy.setSetting4(JSON.toJSONString(setting4));
        }

        StrategyVo.Setting5Entity setting5 = strategyVo.getSetting5();
        if (setting5 != null) {
            strategy.setSetting5(JSON.toJSONString(setting5));
        }

        StrategyVo.Setting6Entity setting6 = strategyVo.getSetting6();
        if (setting6 != null) {
            strategy.setSetting6(JSON.toJSONString(setting6));
        }

        if (strategyVo.getId() != null) {
            //修改
            strategy.setId(strategyVo.getId());
            if (strategy.updateById()) {
                return new ApiResult(Status.SUCCESS, strategy.getId());
            } else {
                return new ApiResult(Status.ERROR);
            }
        } else {
            if (strategy.insert()) {
                return new ApiResult(Status.SUCCESS, strategy.getId());
            } else {
                return new ApiResult(Status.ERROR);
            }
        }
    }

    @Override
    public ApiResult strategyList(String uid) {
        try {
            Wrapper<Strategy> strategyWrapper = new EntityWrapper<>();
            strategyWrapper.eq("user_id", uid);
            Strategy strategy = new Strategy();
            List<Strategy> strategyList = strategy.selectList(strategyWrapper);
            return new ApiResult(Status.SUCCESS, strategyList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ApiResult(Status.ERROR);
    }

    @Override
    public ApiResult simpleStrategyList(String uid) {
        try {
            Wrapper<Strategy> strategyWrapper = new EntityWrapper<>();
            strategyWrapper.eq("user_id", uid);
            strategyWrapper.setSqlSelect("id,strategy_name as strategyName");
            Strategy strategy = new Strategy();
            List<Strategy> strategyList = strategy.selectList(strategyWrapper);
            return new ApiResult(Status.SUCCESS, strategyList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ApiResult(Status.ERROR);
    }

    @Override
    public ApiResult getStrategyById(int id, String uid) {
        try {
            Wrapper<Strategy> strategyWrapper = new EntityWrapper<>();
            strategyWrapper.eq("user_id", uid);
            strategyWrapper.eq("id", id);
            Strategy strategy = new Strategy();
            Strategy selectOne = strategy.selectOne(strategyWrapper);
            return new ApiResult(Status.SUCCESS, selectOne);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("查询单个策略发生异常{}", e.getMessage());
            return new ApiResult(Status.ERROR);
        }
    }

    @Override
    public ApiResult deleteStrategy(int id, String uid) {

        try {
            Strategy strategy = new Strategy();
            Wrapper<Strategy> strategyWrapper = new EntityWrapper<>();
            strategyWrapper.eq("id", id);
            strategyWrapper.eq("user_id", uid);
            boolean delete = strategy.delete(strategyWrapper);
            if (delete) {
                return new ApiResult(Status.SUCCESS, "删除成功");
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("删除策略异常{}", e.getMessage());
        }

        return new ApiResult(Status.ERROR, "删除失败");
    }

}
