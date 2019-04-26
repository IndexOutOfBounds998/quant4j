package com.qklx.qt.admin.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.qklx.qt.admin.dao.SymbolMapper;
import com.qklx.qt.admin.entity.Symbol;
import com.qklx.qt.admin.service.SymbolService;
import com.qklx.qt.core.enums.Status;
import com.qklx.qt.core.api.ApiResult;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author yang
 * @since 2019-04-17
 */
@Service
public class SymbolServiceImpl extends ServiceImpl<SymbolMapper, Symbol> implements SymbolService {

    @Override
    public ApiResult getSymbols() {
        Symbol symbol = new Symbol();
        List<Symbol> symbols = symbol.selectAll();
        return new ApiResult(Status.SUCCESS, symbols);
    }
}
