package de.htwg.cloud.qrcode.app.generator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping
public class QrCodeApi {

    private final QrCodeService service;

    public QrCodeApi(QrCodeService service) {
        this.service = service;
    }

    @GetMapping(path = "/qr-code")
    public String hello() {
        return "QR microservice works! :)  - path: '/qr-code'";
    }

    @PostMapping(path = "/secure/qr-code", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> generate(@RequestBody QrCodeApiDto dto) throws IOException, URISyntaxException, InterruptedException, ExecutionException {
        log.info("Endpoint: /secure/qr-code. Text: " + dto.text());

        if (dto.text() == null || dto.text().isBlank()
            || dto.localId() == null || dto.localId().isBlank()
            || dto.tenantId() == null || dto.tenantId().isBlank()) {
            log.info("Bad Data received: {} {} {}", dto.text(), dto.localId(), dto.tenantId());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        var base64QrCode = service.generate(dto.text(), dto.tenantId());
        log.info("QR Code generated. Sending to history service...");

        // Send Data to History Microservice
        service.sendToHistoryServiceAsync(base64QrCode, dto.tenantId, dto.localId);

        return ResponseEntity
                .ok()
                .body(base64QrCode);
    }


    /**
     * For Internal Testing Purposes
     */
    @PostMapping(path = "/internal-image", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<InputStreamResource> generateQrWithLogoInternal(@RequestBody QrCodeApiDto dto) throws IOException {
        var os = service.generateQrWithLogoInternal(dto.text());
        int outputSize = os.size();

        byte[] qrCodeBytes = os.toByteArray();

        String name = "qr-code.png";
        String fileName = "filename=\"" + name + "\"";
        String fileNameAsterisk = "filename*=UTF-8''" + URLEncoder.encode(name, StandardCharsets.UTF_8);

        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; " + fileName + "; " + fileNameAsterisk)
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(outputSize))
                .contentType(MediaType.IMAGE_PNG)
                .body(new InputStreamResource(new ByteArrayInputStream(qrCodeBytes)));
    }

    private record QrCodeApiDto(
            String text,
            String localId,
            String tenantId
    ) {
    }
}
