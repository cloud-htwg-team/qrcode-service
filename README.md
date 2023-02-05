# QR Code microservise 
- `JAVA 17 + SpringBoot 3`
- QR Code library
  - https://github.com/nayuki/QR-Code-generator
  
## Usage
```
// returns dummy string just to verify its working
GET http://localhost:8080/
```

- Generate QR code, with inside history service call
```
Request
POST http://localhost:8080/qr-code
Content-Type: application/json

{
	"text": "HTWG",
	"localId": "<user-id-from-auth-service>",
	"tenantId": "<tenant-id-from-auth-service>"
}

Response
Content-Type	text/plain;charset=UTF-8
<Base-64-coded-qr-image>
```
