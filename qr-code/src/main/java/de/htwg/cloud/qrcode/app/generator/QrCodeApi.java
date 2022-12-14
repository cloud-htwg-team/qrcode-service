package de.htwg.cloud.qrcode.app.generator;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@RequestMapping("/qr-code")
public class QrCodeApi {

    private final QrCodeService service;

    public QrCodeApi(QrCodeService service) {
        this.service = service;
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public void generate(@RequestBody QrCodeApiDto dto) throws IOException {
        service.generate(dto.text);
    }

    private record QrCodeApiDto(String text) {
    }
}
