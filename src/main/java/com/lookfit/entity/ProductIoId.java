package com.lookfit.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ProductIoId implements Serializable {
    private Integer iono;
    private String pID;
    private Integer orderno;
}
