package com.practica04.init.component;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class PDFLoaderComponent {

    private final TokenTextSplitter splitter;

    public PDFLoaderComponent() {
        this.splitter = TokenTextSplitter.builder()
                .withChunkSize(500)
                .withMinChunkSizeChars(350)
                .withMinChunkLengthToEmbed(10)
                .withMaxNumChunks(100)
                .build();
    }

    public List<Document> obtenerChunks() {
        try {
            ResourcePatternResolver resolver =
                    new PathMatchingResourcePatternResolver();
            Resource[] pdfs =
                    resolver.getResources("classpath:/static/pdfs/*.pdf");
            return splitter.split(
                    Arrays.stream(pdfs)
                            .map(pdf -> new Document(
                                    extraerTexto(pdf),
                                    Map.of("file_path", pdf.getFilename(), "documento", pdf.getFilename())
                            ))
                            .toList()
            );
        } catch (IOException e) {
            throw new RuntimeException("Error leyendo PDFs", e);
        }
    }

    private String extraerTexto(Resource pdf) {
        try (InputStream inputStream = pdf.getInputStream();
             PDDocument doc = Loader.loadPDF(inputStream.readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Error leyendo PDF: " + pdf.getFilename(), e
            );
        }
    }

    public String obtenerDocumentoTexto(String path) {
        Resource resource = new ClassPathResource("static/pdfs/" + path);
        if (resource.exists()) {
            return extraerTexto(resource);
        }
        return "";
    }

}
