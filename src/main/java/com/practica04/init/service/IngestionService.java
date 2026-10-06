package com.practica04.init.service;

import com.practica04.init.component.PDFLoaderComponent;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
public class IngestionService {

    private final PDFLoaderComponent pdfLoaderComponent;
    private final VectorStore vectorStore;

    public IngestionService(PDFLoaderComponent pdfLoaderComponent, VectorStore vectorStore) {
        this.pdfLoaderComponent = pdfLoaderComponent;
        this.vectorStore = vectorStore;
    }

    public void ingestarDocumentos() {
        vectorStore.add(pdfLoaderComponent.obtenerChunks());
    }

    public void eliminarDocumentos() {
        vectorStore.delete("id!=''");
    }

}
