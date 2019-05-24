package com.quant.common.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Option {
    String size;

    String period;
}
