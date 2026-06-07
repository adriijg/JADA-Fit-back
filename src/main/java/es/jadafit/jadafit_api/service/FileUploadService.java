package es.jadafit.jadafit_api.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileUploadService {

    private final Path uploadDir = Paths.get("uploads");

    public FileUploadService() {
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear el directorio de uploads", e);
        }
    }

    public String saveImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        if (extension.isEmpty()) {
            extension = ".jpg";
        }

        String filename = UUID.randomUUID().toString() + extension;

        try {
            Files.copy(file.getInputStream(), uploadDir.resolve(filename));
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el archivo", e);
        }

        return "/api/uploads/" + filename;
    }

    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return;

        String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        try {
            Files.deleteIfExists(uploadDir.resolve(filename));
        } catch (IOException ignored) {
        }
    }
}
