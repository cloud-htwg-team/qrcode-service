package de.htwg.cloud.qrcode.app.generator;

import de.htwg.cloud.qrcode.app.lib.QrCodeLibraryUtil;
import io.nayuki.qrcodegen.QrCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Slf4j
@Component
public class QrCodeService {

    public void generate(String textToEncode) throws IOException {
        QrCode qrCode = QrCode.encodeText(textToEncode, QrCode.Ecc.HIGH);

        BufferedImage img = QrCodeLibraryUtil.toImage(qrCode, 10, 15);
        ImageIO.write(img, "png", new File("qr-code.png"));
    }

}
