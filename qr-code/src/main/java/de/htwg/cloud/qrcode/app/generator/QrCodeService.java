package de.htwg.cloud.qrcode.app.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.htwg.cloud.qrcode.app.lib.QrCodeLibraryUtil;
import io.nayuki.qrcodegen.QrCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

import static org.springframework.http.HttpHeaders.ACCEPT;
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
    private final String tenantServiceServer;
    private final String tenantServicePort;

    public QrCodeService(
            @Value("${history.service.server}") String historyServiceServer,
            @Value("${history.service.port}") String historyServicePort,
            @Value("${tenant.service.server}") String tenantServiceServer,
            @Value("${tenant.service.port}") String tenantServicePort
    ) {
        this.historyServiceServer = historyServiceServer;
        this.historyServicePort = historyServicePort;
        this.tenantServiceServer = tenantServiceServer;
        this.tenantServicePort = tenantServicePort;
    }

    public String generate(String textToEncode, String tenantId) throws IOException, URISyntaxException, InterruptedException {
        QrCode qrCode = QrCode.encodeText(textToEncode, QrCode.Ecc.HIGH);
        BufferedImage img = QrCodeLibraryUtil.toImage(qrCode, 10, 10); // border = whitespace around of qrcode
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        String tenantLogoBase64 = getTenantLogo(tenantId);

        if (tenantLogoBase64 == null || tenantLogoBase64.isBlank()) {
            log.info("Producing pure qr code without logo...");
            ImageIO.write(img, "png", os);
        } else {
            log.info("Attempting to add logo to generated qr code ...");

            byte[] decodedLogo = Base64.getDecoder().decode(tenantLogoBase64);
            ByteArrayInputStream bis = new ByteArrayInputStream(decodedLogo);

            BufferedImage resized = QrCodeLibraryUtil.resize(ImageIO.read(bis), img.getWidth(), img.getHeight());

            //Draw the new image
            BufferedImage combined = new BufferedImage(img.getHeight(), img.getWidth(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = (Graphics2D) combined.getGraphics();
            g.drawImage(img, 0, 0, null);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
            g.drawImage(resized, 0, 0, null);

            log.info("Producing qr code with logo...");
            ImageIO.write(combined, "png", os);

        }

        byte[] qrCodeBytes = os.toByteArray();

        return Base64.getEncoder().encodeToString(qrCodeBytes);
    }

    private String getTenantLogo(String tenantId) throws URISyntaxException, IOException, InterruptedException {
        //  /secure/tenants/{tenantId}/logo
        URI tenantServiceURI = new URI("http://%s:%s/secure/tenants/%s/logo".formatted(
                tenantServiceServer,
                tenantServicePort,
                tenantId
        ));

        log.info(tenantServiceURI.toString());

        HttpRequest tenantServiceGETRequest = HttpRequest.newBuilder()
                .uri(tenantServiceURI)
                .header(ACCEPT, APPLICATION_JSON_VALUE)
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(tenantServiceGETRequest, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            log.info("Tenant service response status: {}", response.statusCode());
            return null;
        }

        return response.body();
    }

    public void sendToHistoryServiceAsync(String base64QrCode, String tenantId, String userId) throws URISyntaxException, IOException, InterruptedException {
        //  /secure/history/tenants/{tenantId}/users/{userId}/entries
        URI historyServiceURI = new URI("http://%s:%s/secure/history/tenants/%s/users/%s/entries".formatted(
                historyServiceServer,
                historyServicePort,
                tenantId,
                userId
        ));

//        log.info(historyServiceURI.toString());

        HistoryDataDto historyDto = new HistoryDataDto(
                Instant.now().getEpochSecond(),
                base64QrCode
        );

        String json = OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(historyDto);
//        log.info(json);

        HttpRequest historyServicePOSTRequest = HttpRequest.newBuilder()
                .uri(historyServiceURI)
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();


        HTTP_CLIENT.sendAsync(historyServicePOSTRequest, HttpResponse.BodyHandlers.ofString())
                .whenComplete((stringHttpResponse, throwable) -> log.info("Response received: {}", stringHttpResponse.body()));
    }

    /**
     * For Testing purposes of generating qr image with logo
     */
    public ByteArrayOutputStream generateQrWithLogoInternal(String textToEncode) throws IOException {
        QrCode qrCode = QrCode.encodeText(textToEncode, QrCode.Ecc.HIGH);
        BufferedImage img = QrCodeLibraryUtil.toImage(qrCode, 10, 10);
        ByteArrayOutputStream os = new ByteArrayOutputStream();

//            byte[] decodedLogo = Base64.getDecoder().decode(tenantLogoBase64);
//            ByteArrayInputStream bis = new ByteArrayInputStream(decodedLogo);

        ClassPathResource classPathResource = new ClassPathResource("demo/logo-120.png");
        var bis = classPathResource.getInputStream();

        BufferedImage overlay = ImageIO.read(bis);

        bis.close();

        var resized = QrCodeLibraryUtil.resize(overlay, img.getWidth(), img.getHeight());

        //Draw the new image
        BufferedImage combined = new BufferedImage(img.getHeight(), img.getWidth(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) combined.getGraphics();
        g.drawImage(img, 0, 0, null);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
        g.drawImage(resized, 0, 0, null);

        ImageIO.write(combined, "png", os);

        return os;
    }

    private record HistoryDataDto(
            long createdAt, // epoch seconds
            String qrCode // base64,
    ) {}

}
