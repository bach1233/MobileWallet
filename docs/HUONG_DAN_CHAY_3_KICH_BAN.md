# Hướng dẫn chạy ba kịch bản đầu của AndroidSecurityLab-REST

Tài liệu này ghi lại cấu hình đang dùng cho máy lab Windows + Android Emulator `emulator-5556`. Cả ba kịch bản chạy trên APK vulnerable:

- Package: `vn.hvktmm.bach.securitylab.rest.vulnerable.debug`
- Backend HTTPS: cổng `8443`
- Burp listener: cổng `4444`
- IP Windows LAN hiện tại: `192.168.0.107`
- Tài khoản lab: `0123456789` / `Bach123`

> `4444` là cổng proxy của Burp. `8443` là cổng API backend. Không đổi hai cổng này cho nhau.

## 1. Bật backend

Mở một cửa sổ **Windows PowerShell** và để cửa sổ đó chạy:

```powershell
Set-Location 'D:\26-27\Đồ Án\AndroidSecurityLab-REST\local-server'
python .\server.py
```

Backend phải in thông báo đang lắng nghe trên `0.0.0.0:8443`. Mở PowerShell thứ hai để kiểm tra:

```powershell
curl.exe -k https://127.0.0.1:8443/status
curl.exe -k https://192.168.0.107:8443/status
```

Cả hai lệnh cần trả JSON có `"status": "OK"`. Nếu IP Windows đổi, thay `192.168.0.107` bằng IPv4 LAN hiện tại lấy từ `ipconfig`; cập nhật IP đích trong route-map `ROUTES` ở đầu `frida-scripts\bypass_root_ssl.js`.

Không chạy thêm bản server thứ hai nếu cổng `8443` đã có tiến trình lắng nghe.

## 2. Cấu hình Burp Suite

Trong Burp mở **Proxy → Proxy settings → Proxy listeners**:

1. Thêm hoặc sửa listener để bind trên **All interfaces (`0.0.0.0`)**, port **`4444`**.
2. Để **Intercept off** trong lúc đăng nhập. Request vẫn được lưu tại **Proxy → HTTP history**, nhưng không bị giữ chờ bấm Forward.
3. Không cần tạo upstream proxy rule riêng cho API. Burp nhận kết nối từ SuperProxy rồi kết nối tới backend theo host trong request.

Nếu máy có firewall Windows, cho phép Burp nhận kết nối vào TCP `4444` từ Android Emulator.

## 3. Cấu hình SuperProxy trên Android

Trong profile đang dùng, nhập các giá trị:

| Trường | Giá trị |
|---|---|
| Protocol | `HTTP` |
| Server | `10.0.2.2` |
| Port | `4444` |
| Authentication | `None` |
| DNS resolution | `Proxy` |

Lưu profile rồi nhấn **Start**. Khi đang chạy, SuperProxy hiện nút **Stop** màu đỏ. Với emulator này, `10.0.2.2` là địa chỉ host Windows nhìn từ Android.

## 4. Bật Frida trên emulator

Mở PowerShell và khai báo đường dẫn dùng trong các lệnh sau:

```powershell
$LabPath = 'D:\26-27\Đồ Án\AndroidSecurityLab-REST'
$AdbPath = 'C:\Users\Admin\scoop\shims\adb.exe'
$FridaPath = 'C:\Users\Admin\AppData\Local\Programs\Python\Python313\Scripts\frida.exe'
$DeviceId = 'emulator-5556'
$Package = 'vn.hvktmm.bach.securitylab.rest.vulnerable.debug'
```

Kiểm tra emulator, quyền root và Frida Server:

```powershell
& $AdbPath devices -l
& $AdbPath -s $DeviceId shell su -c id
& $AdbPath -s $DeviceId shell su -c 'pidof frida-server'
& $FridaPath --version
```

Emulator cần trả `uid=0(root)`. Frida Server cần đang chạy và cùng phiên bản với Frida client (`17.9.10` trong cấu hình này). Nếu `pidof frida-server` không trả PID, bật server đã cài ở `/data/local/tmp/frida-server`:

```powershell
& $AdbPath -s $DeviceId shell "su -c 'nohup /data/local/tmp/frida-server >/dev/null 2>&1 &'"
```

Nếu báo không tìm thấy file, cần chép đúng bản Frida Server cho ABI `x86_64` vào `/data/local/tmp/frida-server`, cấp quyền chạy rồi bật lại. Không cần cài lại mỗi lần nếu server vẫn còn chạy.

Để Frida đọc script ổn định, chép script sang thư mục Temp không dấu trước khi chạy:

```powershell
Copy-Item -LiteralPath "$LabPath\frida-scripts\bypass_root.js" -Destination "$env:TEMP\bypass_root.js" -Force
```

Trước mỗi kịch bản mới, dừng phiên Frida cũ bằng `Ctrl+C`, rồi force-stop app:

```powershell
& $AdbPath -s $DeviceId shell am force-stop $Package
```

Dùng `-f` để Frida khởi chạy app mới và cài hook trước khi các kiểm tra bảo mật chạy.

## 5. Kịch bản 1 — Root detection

Chạy riêng script root:

```powershell
Copy-Item -LiteralPath "$LabPath\frida-scripts\bypass_root.js" -Destination "$env:TEMP\bypass_root.js" -Force
& $FridaPath -U -f $Package -l "$env:TEMP\bypass_root.js"
```

Kiểm tra console có log `RootDetector.isDeviceRooted(Context) hooked` và `Root detection bypass`. Trên màn hình đăng nhập, cảnh báo ROOT phải biến mất. Nếu Frida vẫn hiện cảnh báo, đó là tín hiệu của `FridaDetector`; script root riêng không tắt kiểm tra Frida.

Kịch bản này kiểm tra root detector trên màn hình đăng nhập, không dùng nó để khẳng định đã đăng nhập qua API.

## 6. Kịch bản 2 — Anti-Frida và anti-hook

Để kiểm tra cảnh báo Frida trên ứng dụng, dùng script riêng:

```powershell
Copy-Item -LiteralPath "$LabPath\frida-scripts\bypass_frida.js" -Destination "$env:TEMP\bypass_frida.js" -Force
& $FridaPath -U -f $Package -l "$env:TEMP\bypass_frida.js"
```

Console cần có `FridaDetector.isFridaPresent() hooked`; khi app kiểm tra dấu hiệu Frida, script trả về `false`.

Các hàm chống hook sâu hơn nằm trong `app/src/main/java/vn/hvktmm/bach/securitylab/anti/HookDetectionManager.kt`. Script gộp hiện có là `frida-scripts\bypass_root_ssl_antifrida_antihook.js`; script này hook `isHookDetected`, `detectFridaServer`, `detectFridaLibraries` và `inspectMaps`. Chạy script đó khi cần khảo sát nhóm kiểm tra này:

```powershell
Copy-Item -LiteralPath "$LabPath\frida-scripts\bypass_root_ssl_antifrida_antihook.js" -Destination "$env:TEMP\bypass_root_ssl_antifrida_antihook.js" -Force
& $FridaPath -U -f $Package -l "$env:TEMP\bypass_root_ssl_antifrida_antihook.js"
```

Trong luồng đăng nhập hiện tại, cảnh báo giao diện gọi `FridaDetector`; chưa coi việc hook `HookDetectionManager` là đã kiểm thử thành công nếu chưa thấy hàm đó được gọi và log bypass tương ứng.

## 7. Kịch bản 3 — SSL pinning qua Burp

Đảm bảo backend đang chạy, Burp listener `0.0.0.0:4444` hoạt động và SuperProxy đang chạy (màn hình hiển thị nút **Stop**). Script gộp root + TLS hiện tại là `frida-scripts\bypass_root_ssl.js`. Hook pin/trust và hostname verifier không lọc hostname hay IP trong luồng OkHttp + Conscrypt của app lab. `ROUTES` chỉ đổi alias `10.0.2.2:8443` sang IP Windows để Burp truy cập backend. Nếu đổi IP backend Windows, cập nhật đích trong `ROUTES`; host/IP khác vẫn đi qua hook TLS mà không cần route-map nếu Burp truy cập được đích đó:

```powershell
Copy-Item -LiteralPath "$LabPath\frida-scripts\bypass_root_ssl.js" -Destination "$env:TEMP\bypass_root_ssl.js" -Force
& $AdbPath -s $DeviceId shell am force-stop $Package
& $FridaPath -U -f $Package -l "$env:TEMP\bypass_root_ssl.js"
```

Nhập `0123456789` / `Bach123`. Nếu bàn phím che nút đăng nhập, đóng bàn phím rồi chạm **ĐĂNG NHẬP** thêm lần nữa. Trong console Frida cần thấy pin bypass, hostname được chấp nhận và log đổi host. Trong Burp mở **Proxy → HTTP history**, tìm `POST /login`.

Kết quả đạt ở tầng HTTP khi Burp hiển thị:

- Request có `Host: 192.168.0.107:8443` (hoặc IP LAN mới nhất).
- Response có `HTTP 200` và JSON chứa token lab.
- Log backend có `POST /login` với mã `200`.

Không đưa giá trị token vào ảnh báo cáo. Nếu Burp hiện `Host: 10.0.2.2:8443` kèm `<no response>`, route-map alias chưa chạy hoặc đang nạp nhầm bản script cũ. `10.0.2.2` dùng làm địa chỉ proxy từ emulator tới Burp; khi Burp mở kết nối upstream đến backend, request phải được đổi sang IP LAN Windows ở cổng `8443`.

## 8. Kiểm tra nhanh khi không bắt được request

- Không có request trong Burp: kiểm tra SuperProxy còn chạy, host `10.0.2.2`, port `4444`, và listener Burp bind `0.0.0.0:4444`.
- Có `POST /login` nhưng `<no response>` và host là `10.0.2.2:8443`: force-stop app, nạp lại `bypass_root_ssl.js` mới rồi spawn bằng `-f`.
- Burp nhận request tới IP LAN nhưng backend không ghi `POST /login`: kiểm tra `curl.exe -k https://<IP-LAN-Windows>:8443/status`, route-map nếu dùng alias emulator, và Windows Firewall.
- Không có log Frida: kiểm tra `frida-server` còn chạy dưới quyền root, đúng ABI/phiên bản, và đã force-stop app trước khi dùng `-f`.
- Backend không trả `/status`: khởi động `python .\server.py` trong thư mục `local-server`; không dùng cổng `4444` cho backend.

## 9. Trạng thái xác minh lúc soạn

- Root detection: payload `01_bypass_root_detection.js` chạy trên `emulator-5556`; Frida xác nhận `isDeviceRooted(Context) -> false`, cảnh báo ROOT biến mất. Ảnh thật: `evidence/runtime/payload_root_detection.png`.
- Anti-Frida: payload `03_bypass_anti_frida_hook.js` chạy; `FridaDetector.isFridaPresent()` trả false và giao diện chỉ còn cảnh báo ROOT. Ảnh thật: `evidence/runtime/payload_antifrida.png`. Các phương thức `HookDetectionManager` đã được hook nhưng chưa ghi nhận lời gọi từ luồng giao diện này.
- SSL pinning: payload `02_bypass_ssl_pinning.js` và script gộp `bypass_root_ssl.js` đã chạy. Frida ghi nhận pin bypass, hostname được chấp nhận và route từ `10.0.2.2` sang `192.168.0.107`. Burp nhận `POST /login` với `HTTP 200` lúc 11:53:35; `/status`, `/profile` và `/transactions` cũng trả `HTTP 200`. App vào màn hình Home; ảnh thật: `evidence/runtime/root_ssl_home.png`.
- Anti-hook sâu (`HookDetectionManager`): script có sẵn nhưng chưa xác minh lời gọi thực tế trong luồng giao diện hiện tại; không ghi là đã bypass thành công cho đến khi thấy log/hành vi tương ứng.
