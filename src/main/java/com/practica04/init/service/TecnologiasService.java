package com.practica04.init.service;

import com.practica04.init.component.PDFLoaderComponent;
import com.practica04.init.model.RespuestaDTO;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TecnologiasService {

    private final VectorStore vectorStore;
    private final ChatClient chatClient;
    private final PDFLoaderComponent pdfLoaderComponent;

    @Value("${system.text}")
    private String systemText;

    public TecnologiasService(VectorStore vectorStore, ChatClient chatClient, PDFLoaderComponent pdfLoaderComponent) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClient;
        this.pdfLoaderComponent = pdfLoaderComponent;
    }

    public List<RespuestaDTO> obtenerRespuesta(String preguntaUsuario) {
        List<Document> chunks = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(preguntaUsuario)
                        .topK(10)
                        .similarityThreshold(0.5)
                        .build()
        );

        if (chunks == null || chunks.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, List<Document>> documentos = chunks.stream()
                .collect(Collectors.groupingBy(
                        d -> d.getMetadata()
                                .getOrDefault("file_path", "desconocido")
                                .toString()
                ));

        List<RespuestaDTO> respuestas = new ArrayList<>();
        for (Map.Entry<String, List<Document>> entry : documentos.entrySet()) {
            String documento = entry.getKey();
            String contexto = entry.getValue().stream()
                    .map(Document::getText)
                    .collect(Collectors.joining("\n\n"));

            PromptTemplate promptTemplate = new PromptTemplate(systemText);
            Message systemMessage = promptTemplate.createMessage(Map.of("contexto", contexto));
            Message userMessage = new UserMessage(preguntaUsuario);
            Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
            RespuestaDTO respuesta = chatClient
                    .prompt(prompt)
                    .call()
                    .entity(RespuestaDTO.class);

            if (respuesta != null) {
                respuesta.setDocumento(documento);
                respuestas.add(respuesta);
            }
        }
        return respuestas;
    }

}
