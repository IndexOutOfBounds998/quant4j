package com.quant.admin.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.quant.admin.dao.AccountMapper;
import com.quant.admin.entity.Account;
import com.quant.admin.service.AccountService;
import com.quant.common.config.VpnProxyConfig;
import com.quant.core.api.ApiClient;
import com.quant.core.enums.Status;
import com.quant.core.response.Accounts;
import com.quant.core.vo.AccountVo;
import com.quant.core.api.ApiResult;
import com.quant.core.response.AccountsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author yang
 * @since 2019-04-08
 */
@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {
    private static Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);

    private static final String type = "spot";


    @Autowired
    VpnProxyConfig vpnProxyConfig;


    @Override
    public ApiResult addOrUpdate(AccountVo account) {
        Account at;
        Integer aid;
        boolean flag;
        ApiClient client = null;
        AccountsResponse<List<Accounts>> response = null;
        if (Objects.nonNull(account.getId())) {
            //修改
            at = new Account();
            at.setName(account.getName().trim());
            at.setId(account.getId());
            at.setInfo(account.getInfo().trim());
            at.setAccessKey(account.getAccessKey().trim());
            at.setSecretKey(account.getSecretKey().trim());
            flag = at.updateById();
            if (flag) {
                return new ApiResult(Status.SUCCESS);
            } else {
                return new ApiResult(Status.USER_NOT_EXIST);
            }
        } else {
            try {
                //通过访问key 查询用户账户id 检查是否可用
                client = new ApiClient(account.getAccessKey(), account.getSecretKey(), vpnProxyConfig);
                response = client.accounts();
            } catch (Exception e) {
                logger.error("api 获取账户的基础信息失败=========" + e.getMessage());
            }
            if (Objects.requireNonNull(response).getStatus().equals("ok")) {
                //请求成功
                List<Accounts> accounts = response.getData();
                if (!accounts.isEmpty()) {
                    //获取现货账户的id
                    Optional<Accounts> optional = accounts.stream().filter(ac -> ac.getType().equals(type)).findFirst();
                    if (optional.isPresent()) {
                        Accounts temp = optional.get();
                        aid = temp.getId();
                        String type, state;
                        type = temp.getType();
                        state = temp.getState();
                        //保存到数据库
                        at = new Account();
                        at.setId(aid);
                        at.setName(account.getName().trim());
                        at.setAccessKey(account.getAccessKey().trim());
                        at.setSecretKey(account.getSecretKey().trim());
                        at.setCreateTime(new Date());
                        at.setUserId(account.getUserId());
                        at.setInfo(account.getInfo());
                        at.setType(type);
                        at.setState(state);
                        try {
                            flag = at.insert();
                        } catch (Exception e) {
                            logger.info("插入新账户失败=============" + e.getMessage());
                            return new ApiResult(Status.Account_maybe_exist);
                        }
                        if (flag) {
                            return new ApiResult(Status.SUCCESS);
                        }
                        return new ApiResult(Status.ERROR);
                    }
                }

            } else {
                //检验失败 返回错误
                return new ApiResult(Status.KEYS_NOT_Available);
            }
        }
        return new ApiResult(Status.ERROR);
    }


    @Override
    public ApiResult selectListByUid(int uid, int page, int limit) {
        try {
            Wrapper<Account> wrapper = new EntityWrapper<>();
            wrapper.eq("user_id", uid);
            wrapper.setSqlSelect("id,info,is_delete as isDelete,name,secret_key as secretKey,user_id as userId,create_time as createTime");//只查询2个字段
            wrapper.orderDesc(Collections.singleton("create_time"));
            Page<Account> accounts = this.selectPage(new Page<>(page, limit), wrapper);
            return new ApiResult(Status.SUCCESS, accounts);
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResult(Status.ERROR, e.getMessage());
        }
    }

    @Override
    public ApiResult accounts(int uid) {
        try {
            Wrapper<Account> wrapper = new EntityWrapper<>();
            wrapper.eq("user_id", uid);
            wrapper.setSqlSelect("id,name");//只查询2个字段
            Account account = new Account();
            List<Account> accounts = account.selectList(wrapper);
            return new ApiResult(Status.SUCCESS, accounts);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("查询账号列表错误{}", e.getMessage());
            return new ApiResult(Status.ERROR);
        }
    }
}
