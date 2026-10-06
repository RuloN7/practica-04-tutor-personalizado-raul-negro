package com.practica04.init.controller;

import com.practica04.init.model.RespuestaDTO;
import com.practica04.init.service.IngestionService;
import com.practica04.init.service.TecnologiasService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("rag")
public class TecnologiasController {

    private final IngestionService ingestionService;
    private final TecnologiasService tecnologiasService;

    public TecnologiasController(IngestionService ingestionService, TecnologiasService tecnologiasService) {
        this.ingestionService = ingestionService;
        this.tecnologiasService = tecnologiasService;
    }

    @PostMapping("ingest")
    public ResponseEntity<String> ingest() {
        ingestionService.ingestarDocumentos();
        return ResponseEntity.ok("Ingesta completa");
    }

    @DeleteMapping("clear")
    public ResponseEntity<String> clear() {
        ingestionService.eliminarDocumentos();
        return ResponseEntity.ok("Documentos eliminados");
    }

    @GetMapping("resultados")
    public ResponseEntity<List<RespuestaDTO>> getResultados(@RequestParam("tecnologia") String tecnologia) {
        return ResponseEntity.ok(tecnologiasService.obtenerRespuesta(tecnologia));
    }

}
