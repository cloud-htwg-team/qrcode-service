package de.htwg.cloud.qrcode.app.generator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URISyntaxException;
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

        String textToEncode = dto.text;
        if (textToEncode == null || textToEncode.isBlank()) {
            log.info("Bad data received: {}", dto.text);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        var base64QrCode = service.generate(textToEncode);

        // Send Data to History Microservice
        service.sendToHistoryServiceAsync(base64QrCode, dto.tenantId, dto.localId);

        return ResponseEntity
                .ok()
                .body(base64QrCode);
    }


    private record QrCodeApiDto(
            String text,
            String localId,
            String tenantId
    ) {
    }
}
