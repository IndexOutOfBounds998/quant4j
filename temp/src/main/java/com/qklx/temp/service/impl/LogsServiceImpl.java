package com.qklx.temp.service.impl;

import com.qklx.temp.entity.Logs;
import com.qklx.temp.dao.LogsMapper;
import com.qklx.temp.service.LogsService;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author yang
 * @since 2019-04-30
 */
@Service
public class LogsServiceImpl extends ServiceImpl<LogsMapper, Logs> implements LogsService {

}
