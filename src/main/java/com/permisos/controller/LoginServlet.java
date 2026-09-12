package com.permisos.controller;

import com.permisos.dao.EmpleadoDAO;
import com.permisos.dao.RolDAO;
import com.permisos.dao.UsuarioDAO;
import com.permisos.model.Empleado;
import com.permisos.model.Rol;
import com.permisos.model.Usuario;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Controlador de autenticación (flujo de EMPLEADO).
 * GET  -> muestra el formulario de login.
 * POST -> valida credenciales y crea la sesión.
 *
 * Nota: el modelo Usuario no trae "nombreRol" ni "nombreEmpleado" como
 * campos de conveniencia, así que aquí se consultan Rol y Empleado por
 * separado y se guardan en sesión, para que las vistas puedan mostrarlos
 * sin tener que repetir esas consultas en cada Servlet.
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final RolDAO rolDAO = new RolDAO();
    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();

    private static final String NOMBRE_ROL_EMPLEADO = "EMPLEADO";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if ("GET".equalsIgnoreCase(request.getMethod())) {
            request.getRequestDispatcher("/vistas/login.jsp").forward(request, response);
            return;
        }

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // Validación de campos obligatorios en el servidor
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            request.setAttribute("error", "Debe ingresar usuario y contraseña.");
            request.getRequestDispatcher("/vistas/login.jsp").forward(request, response);
            return;
        }

        try {
            Usuario usuario = usuarioDAO.autenticar(username.trim(), password);

            if (usuario == null) {
                request.setAttribute("error", "Usuario o contraseña incorrectos.");
                request.getRequestDispatcher("/vistas/login.jsp").forward(request, response);
                return;
            }

            Rol rol = rolDAO.buscarPorId(usuario.getIdRol());
            Empleado empleado = empleadoDAO.buscarPorId(usuario.getIdEmpleado());

            usuarioDAO.registrarUltimoAcceso(usuario.getIdUsuario());

            HttpSession session = request.getSession(true);
            session.setAttribute("usuario", usuario);
            session.setAttribute("rol", rol);
            session.setAttribute("empleado", empleado);

            // Este Servlet cubre el flujo de EMPLEADO; si el rol no es
            // EMPLEADO, igual lo dejamos pasar a /solicitudes por ahora
            // (los otros roles los manejan los Servlets de sus compañeros).
            response.sendRedirect(request.getContextPath() + "/solicitudes");

        } catch (SQLException e) {
            request.setAttribute("error", "Error de conexión con la base de datos.");
            request.getRequestDispatcher("/vistas/login.jsp").forward(request, response);
        }
    }
}
