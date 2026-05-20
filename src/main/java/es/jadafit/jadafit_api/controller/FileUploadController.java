package es.jadafit.jadafit_api.controller;

import es.jadafit.jadafit_api.service.FileUploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    private final FileUploadService fileUploadService;

    public FileUploadController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    @PostMapping("/image")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        String url = fileUploadService.saveImage(file);
        return ResponseEntity.ok(Map.of("url", url));
    }
}
