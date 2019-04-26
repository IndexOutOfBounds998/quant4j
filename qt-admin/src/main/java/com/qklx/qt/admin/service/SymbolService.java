package com.qklx.qt.admin.service;

import com.baomidou.mybatisplus.service.IService;
import com.qklx.qt.admin.entity.Symbol;
import com.qklx.qt.core.api.ApiResult;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author yang
 * @since 2019-04-17
 */
public interface SymbolService extends IService<Symbol> {

    ApiResult getSymbols();
}
