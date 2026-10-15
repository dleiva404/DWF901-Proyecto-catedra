package com.permisos.controller;

import java.io.IOException;
import java.net.URLEncoder;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet(name = "VerificarServlet", urlPatterns = {"/verificar"})
public class VerificarServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String destino = request.getContextPath() + "/verificar.xhtml";
        String token = request.getParameter("token");
        if (token != null && !token.trim().isEmpty()) {
            destino += "?token=" + URLEncoder.encode(token.trim(), "UTF-8");
        }
        response.sendRedirect(destino);
    }
}