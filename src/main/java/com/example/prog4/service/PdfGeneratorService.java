package com.example.prog4.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;

@Service
@AllArgsConstructor
public class PdfGeneratorService {
    private final TemplateEngine templateEngine;


    public byte[] generatePdfFromHtmlTemplate(String templateName, Context context) {
        try {
            String html = templateEngine.process(templateName, context);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(outputStream);
            renderer.finishPDF();
            outputStream.close();

            return outputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            // Handle exceptions appropriately
            return null;
        }
    }
}
