package com.mateuspaz.reservas.support;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.mateuspaz.reservas.reserva.persistence.ReservaRepository;
import com.mateuspaz.reservas.reserva.persistence.ResultadoRequisicaoRepository;
import com.mateuspaz.reservas.reserva.persistence.VagaRepository;

@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class PostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("reservas")
            .withUsername("reservas")
            .withPassword("reservas");

    @Autowired
    protected VagaRepository vagaRepository;

    @Autowired
    protected ReservaRepository reservaRepository;

    @Autowired
    protected ResultadoRequisicaoRepository resultadoRequisicaoRepository;

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @BeforeEach
    void clearDatabase() {
        resultadoRequisicaoRepository.deleteAll();
        reservaRepository.deleteAll();
        vagaRepository.deleteAll();
    }
}
