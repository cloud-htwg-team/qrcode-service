package de.htwg.cloud.qrcode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static de.htwg.cloud.qrcode.app.generator.QrCodeApi.QR_CODE_GENERATOR_REST_PATH;

@RestController
@SpringBootApplication
public class QrcodeApplication {

	@GetMapping("/")
	public String hello() {
		return "QR microservice works! :)";
	}

	public static void main(String[] args) {
		SpringApplication.run(QrcodeApplication.class, args);
	}

}
