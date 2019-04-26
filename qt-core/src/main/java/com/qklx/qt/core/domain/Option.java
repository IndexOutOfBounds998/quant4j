package com.qklx.qt.core.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Option {
    String size;

    String period;
}
