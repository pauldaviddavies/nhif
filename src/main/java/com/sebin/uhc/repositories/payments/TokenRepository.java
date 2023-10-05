package com.sebin.uhc.repositories.payments;

import com.sebin.uhc.entities.onboarding.Beneficiaries;
import com.sebin.uhc.entities.payments.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token,Long> {

    @Query(value = "select * from token where type=?1 order by id desc limit 1",nativeQuery = true)
    Optional<Token> findByTokenTypeDesc(String type);
}
