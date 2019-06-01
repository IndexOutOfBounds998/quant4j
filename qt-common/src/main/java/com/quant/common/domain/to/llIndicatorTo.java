package com.quant.common.domain.to;

import com.quant.common.domain.vo.BaseInfoEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by yang on 2019/5/28.
 */
@NoArgsConstructor
@Data
public class llIndicatorTo {
    private Integer id;
    private BaseInfoEntity baseInfo;
    private BuyAndSellIndicatorTo baseData;

}
