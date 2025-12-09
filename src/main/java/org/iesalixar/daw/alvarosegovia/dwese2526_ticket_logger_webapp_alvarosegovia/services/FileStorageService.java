package org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    /**
     * Ruta raíz donde se guardarán los archivos.
     * Ej: /app/uploads
     */
    @Value("${app.upload-root}")
    private String uploadRootPath;

    /**
     * Guarda un archivo en la ruta configurada.
     */
    public String saveFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            logger.warn("Intento de guardar un archivo vacío o nulo");
            return null;
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);

            // Nombre único
            String uniqueFileName = UUID.randomUUID().toString();
            if (!extension.isBlank()) {
                uniqueFileName += "." + extension;
            }

            // Ruta base SIN subcarpetas
            Path uploadDirPath = Paths.get(uploadRootPath);

            // Crear directorio si no existe
            Files.createDirectories(uploadDirPath);

            // Ruta final
            Path filePath = uploadDirPath.resolve(uniqueFileName);

            Files.write(filePath, file.getBytes());

            logger.info("Archivo {} guardado exitosamente en {}", uniqueFileName, filePath);

            // URL accesible (sirviéndose desde static-locations)
            return "/" + uniqueFileName;

        } catch (IOException e) {
            logger.error("Error guardando archivo: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Elimina un archivo previamente guardado.
     */
    public void deleteFile(String filePathOrWebPath) {
        if (filePathOrWebPath == null || filePathOrWebPath.isBlank()) {
            logger.warn("Se intentó eliminar un archivo con ruta vacía.");
            return;
        }

        try {
            String fileName = normalizeFileName(filePathOrWebPath);

            Path uploadDirPath = Paths.get(uploadRootPath);
            Path fileToDelete = uploadDirPath.resolve(fileName);

            Files.deleteIfExists(fileToDelete);
            logger.info("Archivo {} eliminado correctamente ({})", fileName, fileToDelete);

        } catch (IOException e) {
            logger.error("Error al eliminar archivo {}: {}", filePathOrWebPath, e.getMessage(), e);
        }
    }

    /**
     * Extrae la extensión de un archivo.
     */
    private String getFileExtension(String fileName) {
        if (fileName != null) {
            int lastDot = fileName.lastIndexOf(".");
            if (lastDot > 0 && lastDot < fileName.length() - 1) {
                return fileName.substring(lastDot + 1);
            }
        }
        return "";
    }

    /**
     * Obtiene solo el nombre del archivo desde una ruta o URL.
     */
    private String normalizeFileName(String input) {
        String value = input.trim();

        // Quitar "/" inicial en URLs
        if (value.startsWith("/")) {
            value = value.substring(1);
        }

        // Si trae una ruta completa, quedarse solo con el nombre
        int lastSlash = value.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash < value.length() - 1) {
            value = value.substring(lastSlash + 1);
        }

        return value;
    }
}
