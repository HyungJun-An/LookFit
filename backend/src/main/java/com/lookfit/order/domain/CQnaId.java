package com.lookfit.order.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CQnaId implements Serializable {
    private Integer cNo;
    private Integer cRef;
}
