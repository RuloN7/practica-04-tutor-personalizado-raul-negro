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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
                        .topK(5)
                        .similarityThreshold(0.5)
                        .build()
        );
        String contexto = chunks.stream()
                .map(d -> {
                    String documento = d.getMetadata()
                            .getOrDefault("file_path", "desconocido")
                            .toString();

                    return """
                    =============================
                    DOCUMENTO: %s
                    CONTENIDO:
                    %s
                    """.formatted(
                            documento,
                            d.getText()
                    );
                })
                .collect(Collectors.joining("\n\n---\n\n"));
        PromptTemplate promptTemplate = new PromptTemplate(systemText);
        Message systemMessage = promptTemplate.createMessage(Map.of("contexto", contexto));
        Message userMessage = new UserMessage(preguntaUsuario);
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

        return Arrays.asList(chatClient.prompt(prompt).call().entity(RespuestaDTO[].class));
    }

}
