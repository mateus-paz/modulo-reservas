package com.mateuspaz.reservas.reserva.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ResultadoRequisicaoRepository extends JpaRepository<ResultadoRequisicaoEntity, UUID> {
}
