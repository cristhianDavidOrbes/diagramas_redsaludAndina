package co.edu.ucc.redsalud;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.library.GeneralCodingRules;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.util.Optional;
import org.springframework.data.repository.Repository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * Reglas ArchUnit del esqueleto RedSalud Andina alineadas con el diagrama vivo {@code redSaludAndina_DDD}.
 *
 * <p>Contextos delimitados cubiertos (3 subdominios -> 9 módulos independientes bajo {@code co.edu.ucc.redsalud}):
 * - Core · Atención Teleclínica: {@code interconsulta}, {@code agenda}, {@code episodio}, {@code receta}.
 * - Soporte · Sync y Analítica: {@code sincronizacion}, {@code analitica}.
 * - Genérico · Acceso, Legado y Aud.: {@code identidad}, {@code legado}, {@code auditoria}.
 *
 * <p>Define explícitamente qué PUEDE hacer y qué NO PUEDE hacer cada contexto delimitado y cada capa hexagonal:
 * 1) Ningún módulo depende de ningún otro módulo ("nada depende de nada entre contextos"; las interacciones
 *    OHS, PL y C/P del diagrama DDD se resuelven por eventos/mensajería asíncrona, nunca por imports cruzados).
 * 2) Arquitectura hexagonal estricta dentro de cada módulo (dominio y aplicación 100% Java puro).
 * 3) Confinamiento estricto de ambas ACL del diagrama (ACL SOAP Nocturno y ACL OIDC/OAuth2), Resilience4j,
 *    caché L1/L2, AWS (S3 KMS, S3 Object Lock, SQS), JPA, seguridad ABAC y observabilidad.
 *
 * <p>Usa {@code allowEmptyShould(true)} y {@code optionalLayer(...)} para validar el esqueleto vacío hoy y
 * fallar inmediatamente en cuanto se escriba cualquier clase que rompa un límite.
 */
@AnalyzeClasses(
        packages = "co.edu.ucc.redsalud",
        importOptions = ImportOption.DoNotIncludeTests.class)
public class ArquitecturaTest {

    private static final String BASE_PACKAGE = "co.edu.ucc.redsalud";



    @ArchTest
    static final ArchRule ningunModuloPuedeDependerDeOtro = slices()
            .matching(BASE_PACKAGE + ".(*)..")
            .should().notDependOnEachOther()
            .allowEmptyShould(true)
            .as("QUÉ NO PUEDE HACER: ningún contexto delimitado puede importar o depender de otro contexto");

    @ArchTest
    static final ArchRule ningunModuloPuedeTenerCiclos = slices()
            .matching(BASE_PACKAGE + ".(*)..")
            .should().beFreeOfCycles()
            .allowEmptyShould(true)
            .as("QUÉ NO PUEDE HACER: prohibido cualquier ciclo de dependencias entre módulos");

    @ArchTest
    static final ArchRule ningunSubpaquetePuedeTenerCiclos = slices()
            .matching(BASE_PACKAGE + ".(**)")
            .should().beFreeOfCycles()
            .allowEmptyShould(true)
            .as("QUÉ NO PUEDE HACER: prohibido cualquier ciclo entre paquetes internos");

    @ArchTest
    static final ArchRule prohibidasRelacionesJpaEntreModulos = fields()
            .that().areDeclaredInClassesThat().areAnnotatedWith(Entity.class)
            .and(esRelacionJpa())
            .should(noApuntarAOtroContexto())
            .allowEmptyShould(true)
            .as("REGLA DE ORO (Bloque 1.4): prohibido @ManyToOne/@OneToMany/@OneToOne/@ManyToMany cruzando módulos");



    @ArchTest
    static final ArchRule dominioSoloPuedeUsarJavaYDominio = classes()
            .that().resideInAPackage("..dominio..")
            .should().onlyDependOnClassesThat().resideInAnyPackage(
                    "java..",
                    "..dominio..")
            .allowEmptyShould(true)
            .as("QUÉ SÍ PUEDE HACER dominio: depender únicamente de Java estándar y de su propio paquete dominio");

    @ArchTest
    static final ArchRule dominioNoPuedeUsarFrameworksNiOtrasCapas = noClasses()
            .that().resideInAPackage("..dominio..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..",
                    "jakarta.persistence..",
                    "jakarta.validation..",
                    "jakarta.servlet..",
                    "jakarta.transaction..",
                    "jakarta.xml..",
                    "javax.xml..",
                    "software.amazon..",
                    "io.awspring..",
                    "io.github.resilience4j..",
                    "com.github.benmanes..",
                    "io.micrometer..",
                    "io.opentelemetry..",
                    "org.slf4j..",
                    "com.fasterxml.jackson..",
                    "..aplicacion..",
                    "..infraestructura..")
            .allowEmptyShould(true)
            .as("QUÉ NO PUEDE HACER dominio: usar Spring, JPA, AWS, Jackson, logs ni conocer aplicación o infraestructura");

    @ArchTest
    static final ArchRule dominioNoPuedeLlevarAnotacionesDeFramework = noClasses()
            .that().resideInAPackage("..dominio..")
            .should().beAnnotatedWith(Entity.class)
            .orShould().beAnnotatedWith(Service.class)
            .orShould().beAnnotatedWith(Component.class)
            .orShould().beAnnotatedWith(org.springframework.stereotype.Repository.class)
            .orShould().beAnnotatedWith(RestController.class)
            .orShould().beAnnotatedWith(PreAuthorize.class)
            .orShould().beAnnotatedWith(enPaquete("org.springframework"))
            .orShould().beAnnotatedWith(enPaquete("jakarta.persistence"))
            .allowEmptyShould(true)
            .as("QUÉ NO PUEDE HACER dominio: anotarse con Spring (@Service, @Component, @PreAuthorize) o JPA (@Entity)");

    @ArchTest
    static final ArchRule puertosEntradaSoloPuedenUsarJavaYDominio = classes()
            .that().resideInAPackage("..aplicacion.puertos.entrada..")
            .should().onlyDependOnClassesThat().resideInAnyPackage(
                    "java..",
                    "..dominio..",
                    "..aplicacion.puertos.entrada..")
            .allowEmptyShould(true)
            .as("QUÉ SÍ PUEDE HACER puertos.entrada: usar Java estándar, dominio y contratos de entrada");

    @ArchTest
    static final ArchRule puertosSalidaSoloPuedenUsarJavaYDominio = classes()
            .that().resideInAPackage("..aplicacion.puertos.salida..")
            .should().onlyDependOnClassesThat().resideInAnyPackage(
                    "java..",
                    "..dominio..",
                    "..aplicacion.puertos.salida..")
            .allowEmptyShould(true)
            .as("QUÉ SÍ PUEDE HACER puertos.salida: usar Java estándar, dominio y contratos de salida");

    @ArchTest
    static final ArchRule serviciosSoloPuedenUsarJavaDominioYPuertos = classes()
            .that().resideInAPackage("..aplicacion.servicios..")
            .should().onlyDependOnClassesThat().resideInAnyPackage(
                    "java..",
                    "..dominio..",
                    "..aplicacion.puertos..",
                    "..aplicacion.servicios..")
            .allowEmptyShould(true)
            .as("QUÉ SÍ PUEDE HACER aplicacion.servicios: orquestar dominio y puertos sin depender de frameworks");

    @ArchTest
    static final ArchRule aplicacionNoPuedeUsarTecnologiaNiInfraestructura = noClasses()
            .that().resideInAPackage("..aplicacion..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..",
                    "jakarta.persistence..",
                    "jakarta.servlet..",
                    "jakarta.transaction..",
                    "jakarta.xml..",
                    "javax.xml..",
                    "software.amazon..",
                    "io.awspring..",
                    "io.github.resilience4j..",
                    "com.github.benmanes..",
                    "io.micrometer..",
                    "io.opentelemetry..",
                    "..infraestructura..")
            .allowEmptyShould(true)
            .as("QUÉ NO PUEDE HACER aplicacion: depender de Spring, JPA, AWS, SOAP ni infraestructura");

    @ArchTest
    static final ArchRule adaptadoresEntradaNoPuedenDependerDeSalidaNiConfig = noClasses()
            .that().resideInAPackage("..infraestructura.entrada..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..infraestructura.salida..",
                    "..infraestructura.config..")
            .allowEmptyShould(true)
            .as("QUÉ NO PUEDE HACER infraestructura.entrada: depender de adaptadores de salida ni configuración");

    @ArchTest
    static final ArchRule entradaSoloPuedeInvocarPuertosDeEntrada = noClasses()
            .that().resideInAPackage("..infraestructura.entrada..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..aplicacion.servicios..",
                    "..aplicacion.puertos.salida..")
            .allowEmptyShould(true)
            .as("QUÉ NO PUEDE HACER infraestructura.entrada: saltarse los puertos de entrada o invocar puertos de salida");

    @ArchTest
    static final ArchRule adaptadoresSalidaNoPuedenDependerDeEntradaNiConfig = noClasses()
            .that().resideInAPackage("..infraestructura.salida..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..infraestructura.entrada..",
                    "..infraestructura.config..")
            .allowEmptyShould(true)
            .as("QUÉ NO PUEDE HACER infraestructura.salida: depender de controladores de entrada ni configuración");

    @ArchTest
    static final ArchRule salidaNoPuedeInvocarPuertosEntradaNiServicios = noClasses()
            .that().resideInAPackage("..infraestructura.salida..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..aplicacion.puertos.entrada..",
                    "..aplicacion.servicios..")
            .allowEmptyShould(true)
            .as("QUÉ NO PUEDE HACER infraestructura.salida: invocar casos de uso ni servicios de aplicación");

    @ArchTest
    static final ArchRule configuracionNoPuedeDependerDeEntrada = noClasses()
            .that().resideInAPackage("..infraestructura.config..")
            .should().dependOnClassesThat().resideInAPackage("..infraestructura.entrada..")
            .allowEmptyShould(true)
            .as("QUÉ NO PUEDE HACER infraestructura.config: depender de controladores o adaptadores de entrada");

    @ArchTest
    static final ArchRule flujoHexagonalPermitidoPorCapas = layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .optionalLayer("Dominio").definedBy("..dominio..")
            .optionalLayer("PuertosEntrada").definedBy("..aplicacion.puertos.entrada..")
            .optionalLayer("PuertosSalida").definedBy("..aplicacion.puertos.salida..")
            .optionalLayer("Servicios").definedBy("..aplicacion.servicios..")
            .optionalLayer("Entrada").definedBy("..infraestructura.entrada..")
            .optionalLayer("Salida").definedBy("..infraestructura.salida..")
            .optionalLayer("Config").definedBy("..infraestructura.config..")
            .whereLayer("Dominio").mayOnlyBeAccessedByLayers(
                    "PuertosEntrada", "PuertosSalida", "Servicios", "Entrada", "Salida", "Config")
            .whereLayer("PuertosEntrada").mayOnlyBeAccessedByLayers("Servicios", "Entrada", "Config")
            .whereLayer("PuertosSalida").mayOnlyBeAccessedByLayers("Servicios", "Salida", "Config")
            .whereLayer("Servicios").mayOnlyBeAccessedByLayers("Config")
            .whereLayer("Entrada").mayNotBeAccessedByAnyLayer()
            .whereLayer("Salida").mayOnlyBeAccessedByLayers("Config")
            .whereLayer("Config").mayNotBeAccessedByAnyLayer()
            .allowEmptyShould(true)
            .as("QUÉ SÍ PUEDE ACCEDER A QUÉ: jerarquía hexagonal estricta hacia el centro del dominio");

    // =========================================================================
    // 3. GUARDRAILS ESPECÍFICOS DEL TALLER (DÓNDE SÍ Y DÓNDE NO VA CADA STACK)
    // =========================================================================

    @ArchTest
    static final ArchRule soapLegadoSoloPuedeUsarseEnLegadoInfraestructura = noClasses()
            .that().resideOutsideOfPackage("..legado.infraestructura..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework.ws..",
                    "jakarta.xml.ws..",
                    "jakarta.xml.bind..",
                    "jakarta.xml.soap..",
                    "javax.xml.soap..")
            .allowEmptyShould(true)
            .as("DDD ACL SOAP Nocturno: SOAP/XML solo puede usarse en legado.infraestructura");

    @ArchTest
    static final ArchRule aclOidcSoloPuedeUsarseEnIdentidadInfraestructura = noClasses()
            .that().resideOutsideOfPackage("..identidad.infraestructura..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework.security.oauth2..")
            .allowEmptyShould(true)
            .as("DDD ACL OIDC / OAuth2: tokens/claims OAuth2 externos solo se traducen en identidad.infraestructura");

    @ArchTest
    static final ArchRule resilienciaYCacheSoloPuedenUsarseEnInfraestructura = noClasses()
            .that().resideOutsideOfPackage("..infraestructura..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "io.github.resilience4j..",
                    "com.github.benmanes.caffeine..",
                    "org.springframework.cache..",
                    "org.springframework.data.redis..")
            .allowEmptyShould(true)
            .as("Bloque 2.2/2.3: Resilience4j, Caffeine y Redis solo pueden usarse en infraestructura");

    @ArchTest
    static final ArchRule awsSqsS3KmsYStreamSoloPuedenUsarseEnInfraestructura = noClasses()
            .that().resideOutsideOfPackage("..infraestructura..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "software.amazon.awssdk..",
                    "io.awspring.cloud..",
                    "org.springframework.cloud.stream..")
            .allowEmptyShould(true)
            .as("Bloque 3.3/4.2/4.3: AWS SDK (S3, SQS, KMS) y Spring Cloud Stream solo pueden usarse en infraestructura");

    @ArchTest
    static final ArchRule entidadesJpaSoloEnInfraestructuraSalida = classes()
            .that().areAnnotatedWith(Entity.class)
            .should().resideInAPackage("..infraestructura.salida..")
            .andShould().haveSimpleNameEndingWith("JpaEntity")
            .allowEmptyShould(true)
            .as("Bloque 1.3: toda @Entity JPA debe vivir en infraestructura.salida y terminar en JpaEntity");

    @ArchTest
    static final ArchRule repositoriosSpringDataSoloEnInfraestructuraSalida = classes()
            .that().areAssignableTo(Repository.class)
            .or().areAnnotatedWith(org.springframework.stereotype.Repository.class)
            .should().resideInAPackage("..infraestructura.salida..")
            .allowEmptyShould(true)
            .as("Bloque 1.3/1.4: los repositorios Spring Data solo pueden vivir en infraestructura.salida");

    @ArchTest
    static final ArchRule adaptadoresJpaYMemoriaAislados = noClasses()
            .that().resideInAPackage("..infraestructura.salida.jpa..")
            .should().dependOnClassesThat().resideInAPackage("..infraestructura.salida.memoria..")
            .allowEmptyShould(true)
            .as("Los adaptadores JPA y memoria no pueden acoplarse entre sí");

    @ArchTest
    static final ArchRule controladoresRestSoloEnEntradaYConNombreController = classes()
            .that().areAnnotatedWith(RestController.class)
            .should().resideInAPackage("..infraestructura.entrada..")
            .andShould().haveSimpleNameEndingWith("Controller")
            .allowEmptyShould(true)
            .as("Los controladores REST solo pueden vivir en infraestructura.entrada y llamarse *Controller");

    @ArchTest
    static final ArchRule controladoresSinJpaNiSeguridadDirecta = noClasses()
            .that().resideInAPackage("..infraestructura.entrada..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "jakarta.persistence..",
                    "jakarta.annotation.security..",
                    "org.springframework.data..",
                    "org.springframework.jdbc..",
                    "org.springframework.security..")
            .allowEmptyShould(true)
            .as("Bloque 4.1: los controladores no acceden a BD/JPA ni evalúan seguridad en la frontera HTTP");

    @ArchTest
    static final ArchRule observabilidadSoloEnInfraestructura = noClasses()
            .that().resideOutsideOfPackage("..infraestructura..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "io.micrometer..",
                    "io.opentelemetry..")
            .allowEmptyShould(true)
            .as("Bloque 4.4: Micrometer y OpenTelemetry solo se instrumentan en infraestructura");



    @ArchTest
    static final ArchRule sinInyeccionPorCampos = GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule sinSalidasEstandar = GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule sinJavaUtilLogging = GeneralCodingRules.NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule sinExcepcionesGenericas = GeneralCodingRules.NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule sinJodaTime = GeneralCodingRules.NO_CLASSES_SHOULD_USE_JODATIME
            .allowEmptyShould(true);

    // =========================================================================
    // HELPERS
    // =========================================================================

    private static DescribedPredicate<JavaField> esRelacionJpa() {
        return new DescribedPredicate<>("anotado con @ManyToOne, @OneToMany, @OneToOne o @ManyToMany") {
            @Override
            public boolean test(JavaField field) {
                return field.isAnnotatedWith(ManyToOne.class)
                        || field.isAnnotatedWith(OneToMany.class)
                        || field.isAnnotatedWith(OneToOne.class)
                        || field.isAnnotatedWith(ManyToMany.class);
            }
        };
    }

    private static DescribedPredicate<JavaAnnotation<?>> enPaquete(String prefijo) {
        return new DescribedPredicate<>("anotación en el paquete " + prefijo) {
            @Override
            public boolean test(JavaAnnotation<?> annotation) {
                return annotation.getRawType().getPackageName().startsWith(prefijo);
            }
        };
    }

    private static ArchCondition<JavaField> noApuntarAOtroContexto() {
        return new ArchCondition<>("no referenciar entidades JPA de otro contexto delimitado") {
            @Override
            public void check(JavaField field, ConditionEvents events) {
                Optional<String> moduloOrigen = extraerModulo(field.getOwner().getPackageName());
                Optional<String> moduloDestino = extraerModulo(field.getRawType().getPackageName());
                if (moduloOrigen.isPresent()
                        && moduloDestino.isPresent()
                        && !moduloOrigen.get().equals(moduloDestino.get())) {
                    events.add(SimpleConditionEvent.violated(
                            field,
                            "Relación JPA prohibida entre módulos: " + moduloOrigen.get()
                                    + " -> " + moduloDestino.get() + " en " + field.getFullName()));
                }
            }
        };
    }

    private static Optional<String> extraerModulo(String packageName) {
        String prefijo = BASE_PACKAGE + ".";
        if (!packageName.startsWith(prefijo)) {
            return Optional.empty();
        }
        String resto = packageName.substring(prefijo.length());
        int punto = resto.indexOf('.');
        return Optional.of(punto > 0 ? resto.substring(0, punto) : resto);
    }
}
