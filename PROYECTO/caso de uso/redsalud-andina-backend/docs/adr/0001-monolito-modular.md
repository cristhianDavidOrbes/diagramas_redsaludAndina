# ADR-001: Monolito modular con contextos delimitados y video gestionado

- **Estado:** Propuesto (17/11/2026) · **Escenarios:** ASR-7, ASR-8, ASR-9 (restringido por ASR-2 y ASR-4)
- **Tensión:** Simplicidad operativa vs. escalabilidad independiente.

## Contexto
RedSalud Andina opera 14 centros primarios y 9 especialistas con pico de 40 teleconsultas simultáneas y presupuesto de nube acotado. Microservicios prematuros multiplican latencia de red, despliegues y costo sin volumen que lo justifique; un monolito sin límites impide aislar el legado (`<= 1` módulo) y reutilizar `>= 80 %` del dominio en futuros canales.

## Decisión
Construir un **monolito modular Spring Boot** con 9 módulos independientes bajo `co.edu.ucc.redsalud.*` (4 Core: `interconsulta`, `agenda`, `episodio`, `receta`; 2 Soporte: `sincronizacion`, `analitica`; 3 Genéricos: `identidad`, `legado`, `auditoria`). Cada módulo posee su esquema Flyway propio en PostgreSQL/Aurora, sin `@ManyToOne` ni imports cruzados, blindado en CI con **32 reglas `ArchUnit`** (`ArquitecturaTest`). La señalización de audio/video se delega a un servicio gestionado regional (WebRTC).

## Consecuencias y verificación
- **Positivas:** un solo artefacto desplegable, límites ejecutables (*build-breaking*), reutilización del dominio `>= 80 %`.
- **Negativas:** todos los módulos escalan juntos (mitigado con Multi-AZ y extracción futura solo si cambia el perfil de carga).
- **Verificación:** `mvn test` ejecuta `ArquitecturaTest` (32 reglas en verde).
