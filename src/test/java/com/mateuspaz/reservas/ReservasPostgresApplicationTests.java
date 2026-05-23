package com.mateuspaz.reservas;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.mateuspaz.reservas.support.PostgresIntegrationTest;

@SpringBootTest
@ActiveProfiles("integration")
class ReservasPostgresApplicationTests extends PostgresIntegrationTest {

    @Test
    void contextLoadsWithPostgresAndFlyway() {
    }
}
