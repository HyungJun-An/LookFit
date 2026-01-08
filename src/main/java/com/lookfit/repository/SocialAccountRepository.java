package com.lookfit.repository;

import com.lookfit.entity.SocialAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SocialAccountRepository extends JpaRepository<SocialAccount, Integer> {

    // provider(google 등)와 providerUserId(구글 고유번호)로 기존 가입 여부 확인
    Optional<SocialAccount> findByProviderAndProviderUserId(String provider, String providerUserId);
}