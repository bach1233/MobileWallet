#!/usr/bin/env python3
"""Small local JSON/HTTPS backend used by the REST copy of MobiWallet."""
import json
import ssl
import uuid
from datetime import datetime, timezone
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path

USERS = {
    "bach1": {"password": "Bach123", "email": "bach1@gmail.com", "phone": "0123456789", "displayName": "Trần Việt Bách1", "balance": 10_000_000, "savingsBalance": 0, "premium": False, "admin": False},
    "0334916475": {"password": "Bach123@", "email": "0334916475@lab.local", "phone": "0334916475", "displayName": "Trần Việt Bách", "balance": 10_000_000, "savingsBalance": 0, "premium": False, "admin": False},
    "alice": {"password": "alice123", "email": "alice@lab.local", "phone": "0900000001", "displayName": "Alice Nguyen", "balance": 10_000_000, "savingsBalance": 0, "premium": False, "admin": False},
    "admin": {"password": "admin123", "email": "admin@lab.local", "phone": "0900000002", "displayName": "Lab Administrator", "balance": 99_000_000, "savingsBalance": 0, "premium": True, "admin": True},
}
TOKENS = {}
TRANSACTIONS = []


def now():
    return datetime.now(timezone.utc).isoformat()


def username_for(identifier):
    value = str(identifier or "").strip().lower()
    for username, user in USERS.items():
        if value in {username.lower(), user["email"].lower(), user["phone"].lower()}:
            return username
    return None


def profile(username):
    user = USERS[username]
    return {"username": username, "email": user["email"], "phone": user["phone"], "displayName": user["displayName"], "balance": user["balance"], "savingsBalance": user.get("savingsBalance", 0), "premium": user["premium"], "admin": user["admin"]}


def tx(sender, recipient, amount, kind, message=""):
    return {"id": "TX-" + uuid.uuid4().hex[:8].upper(), "sender": sender, "recipient": recipient, "amount": int(amount), "timestamp": now(), "status": "SUCCESS", "type": kind, "message": message}


class Handler(BaseHTTPRequestHandler):
    def log_message(self, fmt, *args):
        print("MOBILAB", fmt % args)

    def reply(self, status, body):
        data = json.dumps(body, ensure_ascii=False).encode()
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(data)))
        self.end_headers()
        self.wfile.write(data)

    def payload(self):
        length = int(self.headers.get("Content-Length", "0"))
        return json.loads(self.rfile.read(length) or b"{}")

    def user_from_token(self):
        token = self.headers.get("Authorization", "").removeprefix("Bearer ")
        return TOKENS.get(token)

    def require_user(self):
        username = self.user_from_token()
        if not username:
            self.reply(401, {"error": "missing or expired lab session"})
        return username

    def do_POST(self):
        if self.path == "/login":
            data = self.payload()
            username = username_for(data.get("username"))
            if not username or data.get("password") != USERS[username]["password"]:
                return self.reply(401, {"error": "invalid credentials"})
            token = "lab-token-" + uuid.uuid4().hex
            TOKENS[token] = username
            return self.reply(200, {"token": token, "profile": profile(username)})

        if self.path == "/register":
            data = self.payload()
            email = str(data.get("email", "")).strip().lower()
            phone = str(data.get("phone", "")).strip()
            username = email.split("@", 1)[0]
            if not email or not phone or not data.get("password") or username in USERS:
                return self.reply(409, {"error": "account already exists or invalid data"})
            USERS[username] = {"password": data["password"], "email": email, "phone": phone, "displayName": data.get("displayName") or username, "balance": 10_000_000, "savingsBalance": 0, "premium": False, "admin": username == "admin"}
            token = "lab-token-" + uuid.uuid4().hex
            TOKENS[token] = username
            return self.reply(201, {"token": token, "profile": profile(username)})

        username = self.require_user()
        if not username:
            return
        data = self.payload()

        if self.path == "/transfer":
            recipient = username_for(data.get("recipient"))
            amount = data.get("amount", 0)
            if not recipient or recipient == username:
                return self.reply(404, {"error": "recipient not found"})
            if not isinstance(amount, int) or amount <= 0:
                return self.reply(400, {"error": "invalid amount"})
            user = USERS[username]
            limit = 50_000_000 if user["admin"] else 10_000_000 if user["premium"] else 2_000_000
            vulnerable = self.headers.get("X-Lab-Mode") == "vulnerable"
            if not vulnerable and (data.get("pin") != "2468" or amount > limit or amount > user["balance"]):
                return self.reply(403, {"error": "server policy denied transfer"})
            if amount > user["balance"]:
                return self.reply(403, {"error": "insufficient balance"})
            user["balance"] -= amount
            USERS[recipient]["balance"] += amount
            item = tx(username, recipient, amount, "Chuyển tiền", data.get("message", ""))
            TRANSACTIONS.insert(0, item)
            return self.reply(200, {"transaction": item, "profile": profile(username)})

        if self.path == "/payment":
            amount = data.get("amount", 0)
            if not isinstance(amount, int) or amount <= 0:
                return self.reply(400, {"error": "invalid amount"})
            user = USERS[username]
            if data.get("pin") != "2468" or amount > user["balance"]:
                return self.reply(403, {"error": "payment denied"})
            user["balance"] -= amount
            item = tx(username, data.get("recipient", "Dịch vụ"), amount, data.get("type", "Thanh toán"), data.get("message", ""))
            TRANSACTIONS.insert(0, item)
            return self.reply(200, {"transaction": item, "profile": profile(username)})

        if self.path == "/topup":
            amount = data.get("amount", 0)
            if not isinstance(amount, int) or amount <= 0 or data.get("pin") != "2468":
                return self.reply(400, {"error": "invalid top-up request"})
            USERS[username]["balance"] += amount
            TRANSACTIONS.insert(0, tx(username, "Ví MobiWallet", amount, "Nạp tiền vào ví"))
            return self.reply(200, {"profile": profile(username)})

        if self.path == "/savings":
            amount = data.get("amount", 0)
            deposit = bool(data.get("deposit"))
            user = USERS[username]
            if not isinstance(amount, int) or amount <= 0 or data.get("pin") != "2468":
                return self.reply(400, {"error": "invalid savings request"})
            if deposit and amount > user["balance"]:
                return self.reply(403, {"error": "insufficient wallet balance"})
            if not deposit and amount > user.get("savingsBalance", 0):
                return self.reply(403, {"error": "insufficient savings balance"})
            user["balance"] += -amount if deposit else amount
            user["savingsBalance"] = user.get("savingsBalance", 0) + amount if deposit else user.get("savingsBalance", 0) - amount
            TRANSACTIONS.insert(0, tx(username, "Túi Thần Tài", amount, "Gửi tiền vào túi" if deposit else "Rút tiền về ví"))
            return self.reply(200, {"profile": profile(username)})

        return self.reply(404, {"error": "not found"})

    def do_GET(self):
        if self.path == "/status":
            return self.reply(200, {"service": "MobiWallet REST Lab API", "status": "OK"})
        username = self.require_user()
        if not username:
            return
        if self.path in ("/wallet/balance", "/profile"):
            return self.reply(200, {"profile": profile(username)})
        if self.path == "/transactions":
            mine = [item for item in TRANSACTIONS if item["sender"] == username or item["recipient"] == username]
            return self.reply(200, {"transactions": mine})
        return self.reply(404, {"error": "not found"})


def main():
    root = Path(__file__).parent
    cert, key = root / "certs" / "lab-server.pem", root / "certs" / "lab-server-key.pem"
    if not cert.exists() or not key.exists():
        raise SystemExit("Create lab certificate first; see local-server/README.md")
    server = ThreadingHTTPServer(("0.0.0.0", 8443), Handler)
    context = ssl.SSLContext(ssl.PROTOCOL_TLS_SERVER)
    context.load_cert_chain(certfile=cert, keyfile=key)
    server.socket = context.wrap_socket(server.socket, server_side=True)
    print("MobiWallet REST HTTPS API listening on https://0.0.0.0:8443")
    server.serve_forever()


if __name__ == "__main__":
    main()
