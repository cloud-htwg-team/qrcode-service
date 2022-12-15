# cloud-app-dev-project
HTWG Cloud Application Development Project

## QR Code microservise 
- `JAVA 17`
- library
  - https://github.com/nayuki/QR-Code-generator
- build Docker image
  - `docker build  -t qrcode_generator .`
- run Docker image
  - `docker run -d -p 8080:8080 qrcode_generator`
  - `-d` is for `detached mode`
  
### Usage
```
// returns dummy string just to verify its working
GET http://localhost:8080/


// returns generated qr code, you can change the text in request's payload
POST http://localhost:8080/qr-code
Content-Type: application/json

{
  "text": "htwg-konstanz.de"
}
```

---

### Potentional QR code libs:
- JS https://github.com/oblakstudio/qr-code-styling
- Python https://www.geeksforgeeks.org/how-to-generate-qr-codes-with-a-custom-logo-using-python/

