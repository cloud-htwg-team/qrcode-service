package de.htwg.cloud.qrcode.app.generator;

import de.htwg.cloud.qrcode.app.lib.QrCodeLibraryUtil;
import io.nayuki.qrcodegen.QrCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
@Component
public class QrCodeService {

    public ByteArrayOutputStream generate(String textToEncode) throws IOException {
        QrCode qrCode = QrCode.encodeText(textToEncode, QrCode.Ecc.HIGH);

        BufferedImage img = QrCodeLibraryUtil.toImage(qrCode, 10, 15);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(img, "png", os);

        return os;
    }

}
