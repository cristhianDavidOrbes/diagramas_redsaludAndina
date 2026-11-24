package co.edu.ucc.redsalud;

import co.edu.ucc.redsalud.agenda.aplicacion.servicios.ReservarCupoService;
import co.edu.ucc.redsalud.agenda.dominio.AgendaException;
import co.edu.ucc.redsalud.agenda.dominio.CitaTelemedicina;
import co.edu.ucc.redsalud.agenda.dominio.CupoEspecialista;
import co.edu.ucc.redsalud.agenda.infraestructura.salida.memoria.CupoMemoriaAdapter;
import co.edu.ucc.redsalud.analitica.aplicacion.servicios.ConsultarIndicadoresService;
import co.edu.ucc.redsalud.analitica.dominio.AnaliticaException;
import co.edu.ucc.redsalud.analitica.dominio.MetricaCentro;
import co.edu.ucc.redsalud.analitica.dominio.TableroAdopcion;
import co.edu.ucc.redsalud.analitica.infraestructura.salida.metricas.MicrometerAnaliticaAdapter;
import co.edu.ucc.redsalud.episodio.aplicacion.servicios.RegistrarEpisodioService;
import co.edu.ucc.redsalud.episodio.dominio.AdjuntoCifrado;
import co.edu.ucc.redsalud.episodio.dominio.EpisodioClinico;
import co.edu.ucc.redsalud.episodio.dominio.EpisodioException;
import co.edu.ucc.redsalud.episodio.infraestructura.salida.memoria.EpisodioMemoriaAdapter;
import co.edu.ucc.redsalud.interconsulta.aplicacion.servicios.GestionarInterconsultaService;
import co.edu.ucc.redsalud.interconsulta.dominio.EstadoDerivacion;
import co.edu.ucc.redsalud.interconsulta.dominio.Interconsulta;
import co.edu.ucc.redsalud.interconsulta.dominio.InterconsultaException;
import co.edu.ucc.redsalud.interconsulta.infraestructura.salida.memoria.InterconsultaMemoriaAdapter;
import co.edu.ucc.redsalud.receta.aplicacion.servicios.EmitirRecetaService;
import co.edu.ucc.redsalud.receta.dominio.FirmaKms;
import co.edu.ucc.redsalud.receta.dominio.RecetaException;
import co.edu.ucc.redsalud.receta.dominio.RecetaMedica;
import co.edu.ucc.redsalud.receta.infraestructura.salida.memoria.RecetaMemoriaAdapter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Instant;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CoreClinicoYAnaliticaTest {

    @Test
    void agendaImpideDobleReservaDeCupo() {
        ReservarCupoService service = new ReservarCupoService(new CupoMemoriaAdapter());
        CupoEspecialista cupo = new CupoEspecialista(
                "CUPO-1", "ESP-1", "CENTRO-01", Instant.now().plusSeconds(3600), false, null, null, 0L);
        service.publicarCupo(cupo);

        CitaTelemedicina cita = service.reservar("CUPO-1", "PAC-1", "INT-1");
        assertThat(cita.pacienteId()).isEqualTo("PAC-1");
        assertThat(cupo.reservado()).isTrue();
        assertThat(cupo.especialistaId()).isEqualTo("ESP-1");
        assertThat(cupo.centroId()).isEqualTo("CENTRO-01");
        assertThat(cupo.interconsultaId()).isEqualTo("INT-1");
        assertThat(cupo.version()).isZero();

        assertThatThrownBy(() -> service.reservar("CUPO-1", "PAC-2", "INT-2"))
                .isInstanceOf(AgendaException.class)
                .hasMessageContaining("doble reserva");
        assertThatThrownBy(() -> service.reservar("NO-EXISTE", "PAC-2", "INT-2"))
                .isInstanceOf(AgendaException.class);
    }

    @Test
    void interconsultaSigueMaquinaDeEstadosEstricta() {
        GestionarInterconsultaService service = new GestionarInterconsultaService(new InterconsultaMemoriaAdapter());
        Interconsulta ic = service.solicitar("INT-1", "PAC-1", "CENTRO-01", "CARDIOLOGIA");
        assertThat(ic.estado()).isEqualTo(EstadoDerivacion.SOLICITADA);
        assertThat(ic.pacienteId()).isEqualTo("PAC-1");
        assertThat(ic.centroOrigenId()).isEqualTo("CENTRO-01");
        assertThat(ic.especialidad()).isEqualTo("CARDIOLOGIA");
        assertThat(ic.creadaEn()).isNotNull();
        assertThat(ic.version()).isZero();

        assertThatThrownBy(() -> service.avanzarEstado("INT-1", EstadoDerivacion.CERRADA))
                .isInstanceOf(InterconsultaException.class);

        service.avanzarEstado("INT-1", EstadoDerivacion.AGENDADA);
        service.avanzarEstado("INT-1", EstadoDerivacion.ATENDIDA);
        Interconsulta cerrada = service.avanzarEstado("INT-1", EstadoDerivacion.CERRADA);
        assertThat(cerrada.estado()).isEqualTo(EstadoDerivacion.CERRADA);

        assertThatThrownBy(() -> service.avanzarEstado("INT-1", EstadoDerivacion.SOLICITADA))
                .isInstanceOf(InterconsultaException.class);
    }

    @Test
    void episodioCifraAdjuntoConKmsYValidaResidencia() {
        EpisodioMemoriaAdapter adapter = new EpisodioMemoriaAdapter("alias/redsalud-kms", "us-east-1");
        RegistrarEpisodioService service = new RegistrarEpisodioService(adapter, adapter, "us-east-1");

        EpisodioClinico ep = service.registrar("EP-1", "PAC-1", "CENTRO-01", "Evolución estable", "TA 120/80");
        EpisodioClinico conAdjunto = service.adjuntarExamen("EP-1", "rx-torax.pdf", "pdf-bytes".getBytes());

        assertThat(ep.episodioId()).isEqualTo("EP-1");
        assertThat(conAdjunto.pacienteId()).isEqualTo("PAC-1");
        assertThat(conAdjunto.centroId()).isEqualTo("CENTRO-01");
        assertThat(conAdjunto.notaEvolucion()).isEqualTo("Evolución estable");
        assertThat(conAdjunto.signosVitales()).isEqualTo("TA 120/80");
        assertThat(conAdjunto.adjunto().kmsKeyId()).isEqualTo("alias/redsalud-kms");
        assertThat(conAdjunto.actualizadoEn()).isNotNull();
        assertThat(conAdjunto.version()).isZero();

        assertThatThrownBy(() -> new AdjuntoCifrado("k", "", "us-east-1", false))
                .isInstanceOf(EpisodioException.class);
        assertThatThrownBy(() -> new AdjuntoCifrado("k", "kms-1", "us-east-1", true))
                .isInstanceOf(EpisodioException.class);
        AdjuntoCifrado otraRegion = new AdjuntoCifrado("k", "kms-1", "eu-west-1", false);
        assertThatThrownBy(() -> otraRegion.validarResidencia("us-east-1"))
                .isInstanceOf(EpisodioException.class);
    }

    @Test
    void recetaFirmadaEsInmutableYSeEnviaAFarmacia() {
        RecetaMemoriaAdapter adapter = new RecetaMemoriaAdapter("alias/redsalud-kms");
        EmitirRecetaService service = new EmitirRecetaService(adapter, adapter, adapter);

        RecetaMedica receta = service.emitir("REC-1", "EP-1", "PAC-1", "ESP-1", "Losartan 50mg cada 12h");
        assertThat(receta.enviadaFarmacia()).isTrue();
        assertThat(receta.firma().hashSha256()).hasSize(64);
        assertThat(receta.episodioId()).isEqualTo("EP-1");
        assertThat(receta.especialistaId()).isEqualTo("ESP-1");
        assertThat(receta.medicamentos()).contains("Losartan");
        assertThat(receta.version()).isZero();
        assertThat(adapter.despachadas()).containsExactly("REC-1");

        assertThatThrownBy(() -> receta.intentarModificar("Otro medicamento"))
                .isInstanceOf(RecetaException.class);
        assertThatThrownBy(() -> new FirmaKms(" ", "kms", Instant.now()))
                .isInstanceOf(RecetaException.class);
    }

    @Test
    void analiticaCalculaSloYRechazaPorcentajeInvalido() {
        MicrometerAnaliticaAdapter adapter = new MicrometerAnaliticaAdapter(new SimpleMeterRegistry());
        ConsultarIndicadoresService service = new ConsultarIndicadoresService(adapter);

        service.registrar(new MetricaCentro("CENTRO-02", 25L, 99.7, 6.0));
        TableroAdopcion tablero = service.obtenerTablero();
        assertThat(tablero.cumpleSloGlobal()).isTrue();
        assertThat(tablero.disponibilidadPromedio()).isGreaterThanOrEqualTo(99.5);

        assertThatThrownBy(() -> new MetricaCentro("CENTRO-X", 1L, 120.0, 5.0))
                .isInstanceOf(AnaliticaException.class);
    }
}
