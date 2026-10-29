package com.lasferias.cooperativa.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lasferias.cooperativa.database.ConexionBD;
import com.lasferias.cooperativa.database.InicializadorBD;
import com.lasferias.cooperativa.model.Usuario;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UsuarioDAOTest {

    @TempDir
    Path carpetaTemporal;

    private Path rutaBd;
    private UsuarioDAO dao;

    @BeforeEach
    void prepararBaseDeDatos() throws SQLException {
        rutaBd = carpetaTemporal.resolve("usuarios-dao.db");
        InicializadorBD.inicializar(rutaBd);
        dao = new UsuarioDAO(rutaBd);
    }

    @Test
    void creaYRecuperaAdministrador() throws SQLException {
        Usuario usuario = new Usuario("admin", "hash-de-prueba");

        long id = dao.crear(usuario);
        Optional<Usuario> recuperado = dao.obtenerAdministrador();

        assertTrue(id > 0);
        assertEquals(id, usuario.getId());
        assertTrue(recuperado.isPresent());
        assertEquals("admin", recuperado.get().getNombreUsuario());
        assertEquals("hash-de-prueba", recuperado.get().getContrasenaHash());
    }

    @Test
    void existeAlgunUsuarioReflejaElEstadoDeLaTabla() throws SQLException {
        assertFalse(dao.existeAlgunUsuario());

        dao.crear(new Usuario("admin", "hash-de-prueba"));

        assertTrue(dao.existeAlgunUsuario());
    }

    @Test
    void rechazaSegundoUsuarioConElMismoNombre() throws SQLException {
        dao.crear(new Usuario("admin", "hash-1"));

        assertThrows(SQLException.class,
            () -> dao.crear(new Usuario("admin", "hash-2")));
        assertEquals(1, contarUsuarios());
    }

    @Test
    void persisteEntreInstanciasDelDao() throws SQLException {
        dao.crear(new Usuario("admin", "hash-de-prueba"));

        UsuarioDAO otraInstancia = new UsuarioDAO(rutaBd);

        assertTrue(otraInstancia.existeAlgunUsuario());
        assertEquals("admin", otraInstancia.obtenerAdministrador()
            .orElseThrow().getNombreUsuario());
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
