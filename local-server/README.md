# Local HTTPS API

This server is local-only and uses fake accounts and balances. Run these commands from this directory:

```powershell
mkdir certs -Force
openssl req -x509 -newkey rsa:2048 -nodes -keyout certs/lab-server-key.pem -out certs/lab-server.pem -days 3650 -subj "/CN=10-0-2-2.sslip.io" -addext "subjectAltName=DNS:10-0-2-2.sslip.io,IP:10.0.2.2"
python server.py
```

The repository already contains a lab-only certificate with a pin compiled into the builds. For an Android emulator, install `certs/lab-server.pem` as a user CA certificate. The server is addressed from the emulator as `https://10-0-2-2.sslip.io:8443`.

Endpoints: `GET /status`, `POST /login`, `GET /wallet/balance`, `GET /profile`, `GET /transactions`, and `POST /transfer`. The REST app uses this local service for the API and SSL-pinning experiment.

Fake accounts: `alice` / `alice123`; `admin` / `admin123`. Fake transfer PIN: `2468`.
