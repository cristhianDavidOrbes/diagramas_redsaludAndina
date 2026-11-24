package co.edu.ucc.redsalud;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("esqueleto")
class RedSaludAndinaApplicationTests {

    @Test
    void contextoDeSpringArrancaEnModoEsqueleto() {
        // Verifica el cableado completo de beans de los 8 contextos sin dependencias externas
    }
}
