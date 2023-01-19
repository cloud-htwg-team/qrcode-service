docker build -t qrcode .
docker tag qrcode eu.gcr.io/qrcode-374515/zeyesm/java-app-qr
docker push eu.gcr.io/qrcode-374515/zeyesm/java-app-qr