package com.permisos.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

// genera el PDF de una constancia aprobada; no usa el request para que lo puedan usar el servlet y la API
public class ConstanciaPdfGenerador {

    // carpetaLogos: ruta fisica de /img/logos; urlVerificacion: enlace que lleva el QR
    public byte[] generar(Constancia constancia, Empleado emp, String carpetaLogos, String urlVerificacion)
            throws IOException, WriterException {

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);

            String fechaActual = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy").format(new Date());
            String empresaEmisora = constancia.getEmpresaEmisora() != null
                    ? constancia.getEmpresaEmisora().trim() : "GRUPO CALMA";

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {

                dibujarLogo(document, cs, carpetaLogos, empresaEmisora);

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
                    double salario = constancia.getSalarioReferencia() != null
                            ? constancia.getSalarioReferencia().doubleValue() : 0.0;
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
                cs.showText(constancia.getInstitucion() != null
                        ? constancia.getInstitucion() : "Los fines que estime convenientes.");
                cs.endText();

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 11);
                cs.newLineAtOffset(50, 390);
                cs.showText("Motivo de solicitud: "
                        + (constancia.getMotivo() != null ? constancia.getMotivo() : "Trámite personal"));
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

                BufferedImage qrImage = generarImagenQR(urlVerificacion, 90, 90);
                PDImageXObject pdImage = LosslessFactory.createFromImage(document, qrImage);

                cs.drawImage(pdImage, 450, 180, 90, 90);

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 8);
                cs.newLineAtOffset(445, 165);
                cs.showText("Escanee para verificar autenticidad");
                cs.endText();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }

    // si no hay logo (o falla al leerlo) se escribe el nombre de la empresa
    private void dibujarLogo(PDDocument document, PDPageContentStream cs,
                             String carpetaLogos, String empresaEmisora) throws IOException {

        if (carpetaLogos != null) {
            File archivoLogo = new File(carpetaLogos, nombreLogo(empresaEmisora));

            if (archivoLogo.exists()) {
                try {
                    PDImageXObject pdLogo = PDImageXObject.createFromFile(archivoLogo.getAbsolutePath(), document);
                    cs.drawImage(pdLogo, 50, 690, 140, 55);
                    return;
                } catch (IOException e) {
                    // se usa el texto de abajo
                }
            }
        }

        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 16);
        cs.newLineAtOffset(50, 730);
        cs.showText(empresaEmisora.toUpperCase());
        cs.endText();
    }

    private String nombreLogo(String empresaEmisora) {
        String emp = empresaEmisora.toLowerCase();

        if (emp.contains("didelco")) {
            return "LogoDidelco.png";
        } else if (emp.contains("copro")) {
            return "LogoCopro.png";
        } else if (emp.contains("dca")) {
            return "LogoDCA-pmg.png";
        } else if (emp.contains("elf")) {
            return "LogoElf.png";
        } else if (emp.contains("propultran")) {
            return "LogoPropultran.png";
        } else if (emp.contains("steel")) {
            return "LogoSteel.png";
        }
        return "logoGC.png";
    }

    private void escribirParrafoJustificado(PDPageContentStream cs, String texto, float x, float y,
                                            float anchoMaximo, float leading) throws IOException {
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