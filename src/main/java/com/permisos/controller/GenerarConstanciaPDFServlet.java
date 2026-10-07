package com.permisos.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.permisos.dao.ConstanciaDAO;
import com.permisos.dao.EmpleadoDAO;
import com.permisos.model.Constancia;
import com.permisos.model.Empleado;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(name = "GenerarConstanciaPDFServlet", urlPatterns = {"/GenerarConstanciaPDFServlet"})
public class GenerarConstanciaPDFServlet extends HttpServlet {

    private ConstanciaDAO constanciaDAO;
    private EmpleadoDAO empleadoDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        constanciaDAO = new ConstanciaDAO();
        empleadoDAO = new EmpleadoDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("empleado") == null) {
            response.sendRedirect(request.getContextPath() + "/vistas/login.jsp");
            return;
        }

        String idParam = request.getParameter("id");
        if (idParam == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID de constancia no proporcionado.");
            return;
        }

        try {
            int idConstancia = Integer.parseInt(idParam);
            Constancia constancia = constanciaDAO.obtenerPorId(idConstancia);

            if (constancia == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Constancia no encontrada.");
                return;
            }

            if (!"APROBADO".equalsIgnoreCase(constancia.getEstado()) && !"APROBADA".equalsIgnoreCase(constancia.getEstado())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "La constancia no está aprobada.");
                return;
            }

            Empleado emp = empleadoDAO.buscarPorId(constancia.getIdEmpleado());

            byte[] pdfBytes = crearPdfConstanciaDinamica(constancia, emp, request);

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=constancia_" + idConstancia + ".pdf");
            response.setContentLength(pdfBytes.length);

            response.getOutputStream().write(pdfBytes);
            response.getOutputStream().flush();

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID de constancia inválido.");
        } catch (Exception e) {
            throw new ServletException("Error al generar el PDF de la constancia", e);
        }
    }

    private byte[] crearPdfConstanciaDinamica(Constancia constancia, Empleado emp, HttpServletRequest request) throws IOException, WriterException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);

            String fechaActual = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy").format(new Date());
            String empresaEmisora = constancia.getEmpresaEmisora() != null ? constancia.getEmpresaEmisora().trim() : "GRUPO CALMA";

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {

                try {
                    String nombreArchivoLogo = "logoGC.png";
                    String empLower = empresaEmisora.toLowerCase();

                    if (empLower.contains("didelco")) {
                        nombreArchivoLogo = "LogoDidelco.png";
                    } else if (empLower.contains("copro")) {
                        nombreArchivoLogo = "LogoCopro.png";
                    } else if (empLower.contains("dca")) {
                        nombreArchivoLogo = "LogoDCA-pmg.png";
                    } else if (empLower.contains("elf")) {
                        nombreArchivoLogo = "LogoElf.png";
                    } else if (empLower.contains("propultran")) {
                        nombreArchivoLogo = "LogoPropultran.png";
                    } else if (empLower.contains("steel")) {
                        nombreArchivoLogo = "LogoSteel.png";
                    } else if (empLower.contains("invercalma") || empLower.contains("calma")) {
                        nombreArchivoLogo = "logoGC.png";
                    }

                    String rutaLogo = request.getServletContext().getRealPath("/img/logos/" + nombreArchivoLogo);
                    java.io.File archivoLogo = new java.io.File(rutaLogo);

                    if (archivoLogo.exists()) {
                        PDImageXObject pdLogo = PDImageXObject.createFromFile(archivoLogo.getAbsolutePath(), document);
                        cs.drawImage(pdLogo, 50, 690, 140, 55);
                    } else {
                        cs.beginText();
                        cs.setFont(PDType1Font.HELVETICA_BOLD, 16);
                        cs.newLineAtOffset(50, 730);
                        cs.showText(empresaEmisora.toUpperCase());
                        cs.endText();
                    }
                } catch (Exception e) {
                    cs.beginText();
                    cs.setFont(PDType1Font.HELVETICA_BOLD, 16);
                    cs.newLineAtOffset(50, 730);
                    cs.showText(empresaEmisora.toUpperCase());
                    cs.endText();
                }

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 9);
                cs.newLineAtOffset(50, 675);
                cs.showText("Departamento de Recursos Humanos");
                cs.endText();

                String tipoConstancia = constancia.getTipo() != null ? constancia.getTipo().toUpperCase() : "LABORAL";
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 14);
                cs.newLineAtOffset(50, 620);
                cs.showText("CONSTANCIA " + tipoConstancia);
                cs.endText();

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 11);
                cs.newLineAtOffset(50, 570);
                cs.showText("A QUIEN INTERESE:");
                cs.endText();

                String nombreCompleto = (emp != null) ? (emp.getNombre() + " " + emp.getApellido()) : "Colaborador";
                String dui = (emp != null && emp.getDui() != null) ? emp.getDui() : "00000000-0";

                String textoPrincipal;
                if (tipoConstancia.contains("SALARIAL") || tipoConstancia.contains("SALARIO")) {
                    double salario = constancia.getSalarioReferencia() != null ? constancia.getSalarioReferencia().doubleValue() : 0.0;
                    textoPrincipal = String.format("Por medio de la presente se hace constar que el/la colaborador(a) %s, " +
                                    "portador(a) del DUI número %s, labora activamente para %s. " +
                                    "Actualmente percibe un salario mensual de $%.2f más las prestaciones de ley correspondientes.",
                            nombreCompleto, dui, empresaEmisora, salario);
                } else {
                    textoPrincipal = String.format("Por medio de la presente se hace constar que el/la colaborador(a) %s, " +
                                    "portador(a) del DUI número %s, labora activamente para %s desempeñando " +
                                    "sus funciones con total profesionalismo y manteniendo una relación laboral estable.",
                            nombreCompleto, dui, empresaEmisora);
                }

                escribirParrafoJustificado(cs, textoPrincipal, 50, 530, 500, 14);

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 11);
                cs.newLineAtOffset(50, 440);
                cs.showText("La presente constancia se expide a petición del interesado para ser presentada ante: ");
                cs.endText();

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 11);
                cs.newLineAtOffset(50, 422);
                cs.showText(constancia.getInstitucion() != null ? constancia.getInstitucion() : "Los fines que estime convenientes.");
                cs.endText();

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 11);
                cs.newLineAtOffset(50, 390);
                cs.showText("Motivo de solicitud: " + (constancia.getMotivo() != null ? constancia.getMotivo() : "Trámite personal"));
                cs.endText();

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 11);
                cs.newLineAtOffset(50, 340);
                cs.showText("Se extiende la presente en la ciudad de San Salvador, a los " + fechaActual + ".");
                cs.endText();

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 11);
                cs.newLineAtOffset(50, 240);
                cs.showText("Recursos Humanos");
                cs.endText();

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 10);
                cs.newLineAtOffset(50, 225);
                cs.showText(empresaEmisora);
                cs.endText();

                String urlVerificacion = request.getRequestURL().toString().replace(request.getRequestURI(), "")
                        + request.getContextPath() + "/verificar?token=" + constancia.getTokenVerificacion();

                BufferedImage qrImage = generarImagenQR(urlVerificacion, 90, 90);
                PDImageXObject pdImage = LosslessFactory.createFromImage(document, qrImage);

                cs.drawImage(pdImage, 450, 180, 90, 90);

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 8);
                cs.newLineAtOffset(445, 165);
                cs.showText("Escanee para verificar autenticidad");
                cs.endText();
            }

            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }

    private void escribirParrafoJustificado(PDPageContentStream cs, String texto, float x, float y, float anchoMaximo, float leading) throws IOException {
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, 11);
        cs.newLineAtOffset(x, y);

        String[] palabras = texto.split(" ");
        StringBuilder lineaActual = new StringBuilder();

        for (String palabra : palabras) {
            String prueba = lineaActual + " " + palabra;
            if (prueba.length() * 6.0 > anchoMaximo) {
                cs.showText(lineaActual.toString().trim());
                cs.newLineAtOffset(0, -leading);
                lineaActual = new StringBuilder(palabra);
            } else {
                lineaActual.append(" ").append(palabra);
            }
        }
        if (lineaActual.length() > 0) {
            cs.showText(lineaActual.toString().trim());
        }
        cs.endText();
    }

    private BufferedImage generarImagenQR(String texto, int ancho, int alto) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(texto, BarcodeFormat.QR_CODE, ancho, alto);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }
}