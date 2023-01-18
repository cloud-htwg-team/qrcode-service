package de.htwg.cloud.qrcode.app.generator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.htwg.cloud.qrcode.app.lib.QrCodeLibraryUtil;
import io.nayuki.qrcodegen.QrCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Component
public class QrCodeService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .findAndRegisterModules();
    //            .configure(WRITE_DATES_AS_TIMESTAMPS, false); // timestamp number vs text
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    private final String historyServiceServer;
    private final String historyServicePort;
    private final String historyServicePath;

    public QrCodeService(@Value("${history.service.server}") String historyServiceServer,
                         @Value("${history.service.port}") String historyServicePort,
                         @Value("${history.service.path}") String historyServicePath) {
        this.historyServiceServer = historyServiceServer;
        this.historyServicePort = historyServicePort;
        this.historyServicePath = historyServicePath;
    }


    public ByteArrayOutputStream generate(String textToEncode) throws IOException {
        QrCode qrCode = QrCode.encodeText(textToEncode, QrCode.Ecc.HIGH);

        BufferedImage img = QrCodeLibraryUtil.toImage(qrCode, 10, 15);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(img, "png", os);

        return os;
    }

    public CompletableFuture<HttpResponse<String>> sendToHistoryServiceAsync(byte[] qrCodeBytes) throws URISyntaxException, JsonProcessingException {
        URI historyServiceURI = new URI("http://%s:%s%s".formatted(
                historyServiceServer,
                historyServicePort,
                historyServicePath
        ));

        String base64QrCode = Base64.getEncoder().encodeToString(qrCodeBytes);

        DemoHistoryApi.HistoryDataDto historyDto = new DemoHistoryApi.HistoryDataDto(
                "some-user-ID",
                Instant.now(),
                base64QrCode
        );

        String json = OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(historyDto);
//        log.info(json);

        HttpRequest historyServicePOSTRequest = HttpRequest.newBuilder()
                .uri(historyServiceURI)
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return HTTP_CLIENT.sendAsync(historyServicePOSTRequest, HttpResponse.BodyHandlers.ofString());
    }

}
