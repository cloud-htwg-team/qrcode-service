package de.htwg.cloud.qrcode.app.generator;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * For testing purposes of QrCodeApi
 */
@RestController
@RequestMapping(DemoHistoryApi.HISTORY_REST_PATH)
public class DemoHistoryApi {

    public static final String HISTORY_REST_PATH = "/history";


    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> receiveData(@RequestBody HistoryDataDto dto) {
        return ResponseEntity
                .accepted()
                .body("Successfully Received: %s, %s, %s".formatted(dto.userId, dto.createdAt, dto.qrCode.length()));
    }

    @GetMapping
    public String hello() {
        return "/history  service works";
    }

    public record HistoryDataDto(
            String userId,
            Instant createdAt,
            String qrCode // base64
    ) {
    }
}
