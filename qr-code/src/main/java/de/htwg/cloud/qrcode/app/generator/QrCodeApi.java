package de.htwg.cloud.qrcode.app.generator;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(QrCodeApi.QR_CODE_GENERATOR_REST_PATH)
public class QrCodeApi {

    public static final String QR_CODE_GENERATOR_REST_PATH = "/qr-code";
    private final QrCodeService service;

    public QrCodeApi(QrCodeService service) {
        this.service = service;
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<InputStreamResource> generate(@RequestBody QrCodeApiDto dto) throws IOException {
        ByteArrayOutputStream os = service.generate(dto.text);
        int outputSize = os.size();

        String name = "qr-code.png";

        String fileName = "filename=\"" + name + "\"";
        String fileNameAsterisk = "filename*=UTF-8''" + URLEncoder.encode(name, StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; " + fileName + "; " + fileNameAsterisk)
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(outputSize))
                .contentType(MediaType.IMAGE_PNG)
                .body(new InputStreamResource(new ByteArrayInputStream(os.toByteArray())));
    }

    private record QrCodeApiDto(String text) {}
}
