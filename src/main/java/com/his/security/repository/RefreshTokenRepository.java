package com.his.security.repository;

import com.his.security.entity.RefreshToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByToken(String refreshToken);

    @Modifying
    @Transactional
    @Query("""
       UPDATE RefreshToken r SET r.activo = false WHERE r.usuario.id = :usuarioId AND r.activo = true
    """)
    void deactivateAllByUserId(@Param("usuarioId") UUID usuarioId);

    @Modifying
    @Transactional
    @Query("""
      UPDATE RefreshToken r SET r.activo = false WHERE r.token = :token AND r.activo = true
    """)
    void deactivateByToken(@Param("token") String token);

}
