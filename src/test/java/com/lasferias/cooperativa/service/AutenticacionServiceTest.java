package com.lasferias.cooperativa.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lasferias.cooperativa.dao.UsuarioDAO;
import com.lasferias.cooperativa.database.ConexionBD;
import com.lasferias.cooperativa.database.InicializadorBD;
import com.lasferias.cooperativa.model.Usuario;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AutenticacionServiceTest {

    @TempDir
    Path carpetaTemporal;

    private Path rutaBd;
    private UsuarioDAO dao;
    private AutenticacionService service;

    @BeforeEach
    void prepararServicio() throws SQLException {
        rutaBd = carpetaTemporal.resolve("autenticacion.db");
        InicializadorBD.inicializar(rutaBd);
        dao = new UsuarioDAO(rutaBd);
        service = new AutenticacionService(dao);
    }

    @Test
    void crearAdministradorGuardaHashYNombreAdmin() throws SQLException {
        service.crearAdministrador("clave123", "clave123");

        assertEquals(1, contarUsuarios());
        Usuario admin = dao.obtenerAdministrador().orElseThrow();
        assertEquals("admin", admin.getNombreUsuario());
        assertNotEquals("clave123", admin.getContrasenaHash());
        assertTrue(admin.getContrasenaHash().startsWith("$2a$"));
    }

    @Test
    void autenticaContrasenaCorrectaYRechazaIncorrecta() throws SQLException {
        service.crearAdministrador("clave123", "clave123");

        assertTrue(service.autenticar("clave123"));
        assertFalse(service.autenticar("otraClave"));
    }

    @Test
    void rechazaContrasenaVacia() throws SQLException {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
            () -> service.crearAdministrador("", ""));

        assertTrue(error.getMessage().contains("obligatoria"));
        assertEquals(0, contarUsuarios());
    }

    @Test
    void rechazaContrasenaMenorDeSeisCaracteres() throws SQLException {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
            () -> service.crearAdministrador("12345", "12345"));

        assertTrue(error.getMessage().contains("6"));
        assertEquals(0, contarUsuarios());
    }

    @Test
    void aceptaContrasenaDeExactamenteSeisCaracteres() throws SQLException {
        service.crearAdministrador("123456", "123456");

        assertTrue(service.autenticar("123456"));
    }

    @Test
    void rechazaConfirmacionDistinta() throws SQLException {
        assertThrows(IllegalArgumentException.class,
            () -> service.crearAdministrador("clave123", "clave124"));

        assertEquals(0, contarUsuarios());
    }

    @Test
    void debeCrearAdministradorConTablaVaciaYFalsoTrasCrear() throws SQLException {
        assertTrue(service.debeCrearAdministrador());

        service.crearAdministrador("clave123", "clave123");

        assertFalse(service.debeCrearAdministrador());
    }

    @Test
    void noPermiteSegundoAdministrador() throws SQLException {
        service.crearAdministrador("clave123", "clave123");

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
            () -> service.crearAdministrador("otra123", "otra123"));

        assertEquals("Ya existe un administrador registrado.", error.getMessage());
        assertEquals(1, contarUsuarios());
    }

    @Test
    void persisteEntreInstanciasDelDao() throws SQLException {
        service.crearAdministrador("clave123", "clave123");

        AutenticacionService otroServicio =
            new AutenticacionService(new UsuarioDAO(rutaBd));

        assertTrue(otroServicio.autenticar("clave123"));
    }

    @Test
    void autenticarSinAdministradorDevuelveFalse() throws SQLException {
        assertFalse(service.autenticar("clave123"));
    }

    @Test
    void loginIncorrectoNoRompeLaBaseDeDatos() throws SQLException {
        service.crearAdministrador("clave123", "clave123");

        assertFalse(service.autenticar("incorrecta"));
        assertTrue(service.autenticar("clave123"));
        assertEquals(1, contarUsuarios());
    }

    @Test
    void unaViolacionUnicaNoSeReportaComoExito() throws SQLException {
        dao.crear(new Usuario("admin", "hash-existente"));
        UsuarioDAO daoQueNoVeElAdministrador = new UsuarioDAO(rutaBd) {
            @Override
            public boolean existeAlgunUsuario() {
                return false;
            }
        };
        AutenticacionService servicioConCarrera =
            new AutenticacionService(daoQueNoVeElAdministrador);

        assertThrows(SQLException.class,
            () -> servicioConCarrera.crearAdministrador("otra123", "otra123"));
        assertEquals(1, contarUsuarios());
    }

    private int contarUsuarios() throws SQLException {
        try (Connection cn = ConexionBD.obtenerConexion(rutaBd);
             Statement st = cn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM usuario")) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
