# AndroidSecurityLab-REST

Đây là bản sao REST của `AndroidSecurityLab`. Source Firebase gốc không bị thay đổi.

## Chạy backend local

```powershell
cd "D:\26-27\Đồ Án\AndroidSecurityLab-REST\local-server"
python .\server.py
```

Backend lắng nghe HTTPS tại `0.0.0.0:8443`. MuMu truy cập bằng `10-0-2-2.sslip.io:8443` (hostname này trỏ về `10.0.2.2` và có trong SAN của chứng thư lab).

Tài khoản mẫu:

```text
0123456789 / Bach123
bach1@gmail.com / Bach123
alice / alice123
admin / admin123
```

Các API nghiệp vụ đều là REST/JSON: `/login`, `/register`, `/profile`, `/wallet/balance`, `/transactions`, `/transfer`, `/topup`, `/payment`, `/savings`.

Burp có thể đặt listener `0.0.0.0:4444`; proxy của MuMu/Super Proxy trỏ tới `10.0.2.2:4444`. Bản vulnerable và hardened dùng cùng backend, khác nhau ở lớp bảo vệ runtime và chính sách lab.
