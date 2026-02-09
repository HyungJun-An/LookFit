package com.lookfit.wishlist.domain;

import lombok.*;

import java.io.Serializable;

/**
 * Wishlist 복합 키
 * Member와 Product의 조합으로 고유 식별
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class WishlistId implements Serializable {

    private String memberId;
    private String productId;
}
