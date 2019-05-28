package com.quant.admin.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.quant.admin.dao.SymbolMapper;
import com.quant.common.domain.entity.Symbol;
import com.quant.admin.service.SymbolService;
import com.quant.common.enums.Status;
import com.quant.core.api.ApiResult;
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
