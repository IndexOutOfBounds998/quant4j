package com.qklx.temp.service.impl;

import com.qklx.temp.entity.Account;
import com.qklx.temp.dao.AccountMapper;
import com.qklx.temp.service.AccountService;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author yang
 * @since 2019-04-26
 */
@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {

}
