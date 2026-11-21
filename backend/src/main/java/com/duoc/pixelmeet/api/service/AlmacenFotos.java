package com.duoc.pixelmeet.api.service;

import com.duoc.pixelmeet.api.exception.RecursoNoEncontradoException;
import com.duoc.pixelmeet.api.exception.ValidacionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Iterator;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Guarda y entrega las fotos subidas a los eventos.
 * <ul>
 *   <li>Solo JPG o PNG, reconocidos por su firma (magic bytes), no por la extension ni el Content-Type.</li>
 *   <li>Tamano maximo 5 MB y dimensiones maximas de 4000x4000 px, revisadas antes de decodificar
 *       (evita "bombas de descompresion").</li>
 *   <li>La imagen se vuelve a codificar: se descartan metadatos (EXIF con GPS) y cualquier contenido
 *       ajeno incrustado (archivos poliglota).</li>
 *   <li>El nombre en disco es un UUID generado por el servidor; el nombre original nunca se usa.</li>
 * </ul>
 */
@Component
public class AlmacenFotos {

    static final long MAX_BYTES = 5L * 1024 * 1024;
    static final int MAX_LADO = 4000;
    private static final byte[] FIRMA_JPG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] FIRMA_PNG = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};
    private static final Pattern NOMBRE_VALIDO = Pattern.compile("^[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}\\.(jpg|png)$");

    private final Path directorio;

    public AlmacenFotos(@Value("${app.media.dir:./data/media}") String directorio) {
        this.directorio = Path.of(directorio).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.directorio);
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo crear el directorio de fotos", ex);
        }
    }

    public record FotoGuardada(String archivo, String contentType) {
    }

    public FotoGuardada guardar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new ValidacionException("archivo", "Selecciona una imagen");
        }
        if (archivo.getSize() > MAX_BYTES) {
            throw new ValidacionException("archivo", "La imagen no puede superar 5 MB");
        }
        byte[] datos;
        try {
            datos = archivo.getBytes();
        } catch (IOException ex) {
            throw new ValidacionException("archivo", "No se pudo leer la imagen");
        }
        String formato;
        if (empiezaCon(datos, FIRMA_JPG)) {
            formato = "jpg";
        } else if (empiezaCon(datos, FIRMA_PNG)) {
            formato = "png";
        } else {
            throw new ValidacionException("archivo", "Solo se permiten imágenes JPG o PNG");
        }
        BufferedImage imagen = decodificar(datos);
        byte[] limpia = recodificar(imagen, formato);
        String nombre = UUID.randomUUID() + "." + formato;
        try {
            Files.write(directorio.resolve(nombre), limpia, StandardOpenOption.CREATE_NEW);
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo guardar la imagen", ex);
        }
        return new FotoGuardada(nombre, "jpg".equals(formato) ? "image/jpeg" : "image/png");
    }

    public byte[] leer(String archivo) {
        try {
            return Files.readAllBytes(resolver(archivo));
        } catch (IOException ex) {
            throw new RecursoNoEncontradoException("Imagen no encontrada");
        }
    }

    public void eliminar(String archivo) {
        try {
            Files.deleteIfExists(resolver(archivo));
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo eliminar la imagen", ex);
        }
    }

    /** Solo acepta nombres generados por el servidor y que queden dentro del directorio (sin path traversal). */
    private Path resolver(String archivo) {
        if (archivo == null || !NOMBRE_VALIDO.matcher(archivo).matches()) {
            throw new RecursoNoEncontradoException("Imagen no encontrada");
        }
        Path ruta = directorio.resolve(archivo).normalize();
        if (!ruta.startsWith(directorio)) {
            throw new RecursoNoEncontradoException("Imagen no encontrada");
        }
        return ruta;
    }

    private static BufferedImage decodificar(byte[] datos) {
        try (ImageInputStream in = ImageIO.createImageInputStream(new ByteArrayInputStream(datos))) {
            Iterator<ImageReader> lectores = ImageIO.getImageReaders(in);
            if (!lectores.hasNext()) {
                throw new ValidacionException("archivo", "La imagen está dañada o no es válida");
            }
            ImageReader lector = lectores.next();
            try {
                lector.setInput(in, true, true);
                int ancho = lector.getWidth(0);
                int alto = lector.getHeight(0);
                if (ancho <= 0 || alto <= 0 || ancho > MAX_LADO || alto > MAX_LADO) {
                    throw new ValidacionException("archivo", "La imagen no puede superar 4000 x 4000 píxeles");
                }
                return lector.read(0);
            } finally {
                lector.dispose();
            }
        } catch (IOException | RuntimeException ex) {
            if (ex instanceof ValidacionException v) {
                throw v;
            }
            throw new ValidacionException("archivo", "La imagen está dañada o no es válida");
        }
    }

    private static byte[] recodificar(BufferedImage imagen, String formato) {
        BufferedImage salida = imagen;
        if ("jpg".equals(formato) && imagen.getType() != BufferedImage.TYPE_INT_RGB) {
            salida = new BufferedImage(imagen.getWidth(), imagen.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = salida.createGraphics();
            try {
                g.drawImage(imagen, 0, 0, java.awt.Color.WHITE, null);
            } finally {
                g.dispose();
            }
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            if (!ImageIO.write(salida, "jpg".equals(formato) ? "jpg" : "png", out)) {
                throw new ValidacionException("archivo", "La imagen está dañada o no es válida");
            }
            return out.toByteArray();
        } catch (IOException ex) {
            throw new ValidacionException("archivo", "La imagen está dañada o no es válida");
        }
    }

    private static boolean empiezaCon(byte[] datos, byte[] firma) {
        return datos.length >= firma.length && Arrays.equals(Arrays.copyOf(datos, firma.length), firma);
    }
}
