# WalletCore

Backend ví điện tử sử dụng **Java 17**, **Spring Boot 4.1.2**, **PostgreSQL**, Spring Data JPA, Spring Security và Flyway.

Tài liệu hướng dẫn cài PostgreSQL trực tiếp trên máy và chạy backend local. Các lệnh Windows bên dưới dùng PowerShell, thực hiện tại thư mục chứa `pom.xml`.

> **Hiện trạng mã nguồn:** phần JWT có hai lỗi cần sửa trước khi build, được mô tả ở mục 3.1. Sau khi ứng dụng chạy và tạo bảng, cần thêm role `USER` theo mục 5 để API đăng ký hoạt động.

## 1. Chuẩn bị

- **JDK 17 trở lên**; dự án cấu hình biên dịch với Java 17.
- **PostgreSQL 17** và công cụ dòng lệnh `psql`.
- Kết nối Internet trong lần chạy Maven đầu tiên để tải Maven và các dependency.

Dự án có Maven Wrapper (`mvnw`, `mvnw.cmd`), không cần cài Maven riêng. Phiên bản Maven được khai báo trong `.mvn/wrapper/maven-wrapper.properties`.

Kiểm tra JDK:

```powershell
java -version
javac -version
```

Nếu máy có nhiều JDK, đặt `JAVA_HOME` trỏ tới thư mục JDK muốn sử dụng.

## 2. Cài và tạo database local

### 2.1. Cài PostgreSQL

1. Tải bộ cài từ [PostgreSQL Downloads](https://www.postgresql.org/download/), chọn hệ điều hành đang dùng. Với Windows, chọn bộ cài PostgreSQL 17.
2. Cài các thành phần **PostgreSQL Server** và **Command Line Tools**.
3. Giữ cổng `5432`, đặt mật khẩu cho tài khoản quản trị `postgres` và ghi nhớ mật khẩu này.
4. Đảm bảo dịch vụ PostgreSQL đang chạy. Trên Windows có thể kiểm tra trong ứng dụng **Services**.
5. Thêm thư mục `bin` của PostgreSQL vào `PATH` nếu bộ cài chưa thêm, ví dụ `C:\Program Files\PostgreSQL\17\bin`, rồi mở lại terminal.

Kiểm tra:

```powershell
psql --version
```

### 2.2. Tạo user và database

Đăng nhập bằng tài khoản quản trị; nhập mật khẩu `postgres` đã đặt lúc cài:

```powershell
psql -h localhost -p 5432 -U postgres -d postgres
```

Chạy lần lượt các lệnh sau trong `psql` cho lần thiết lập đầu tiên:

```sql
CREATE USER walletcore WITH PASSWORD '123456';
CREATE DATABASE walletcore OWNER walletcore;
\q
```

Đây là thông tin kết nối local khớp với `src/main/resources/application.yml`:

| Thuộc tính | Giá trị |
| --- | --- |
| Host | `localhost` |
| Port | `5432` |
| Database | `walletcore` |
| Username | `walletcore` |
| Password local | `123456` |
| JDBC URL | `jdbc:postgresql://localhost:5432/walletcore` |

Kiểm tra kết nối bằng user vừa tạo, nhập mật khẩu `123456`:

```powershell
psql -h localhost -p 5432 -U walletcore -d walletcore
```

Trong `psql`:

```sql
\conninfo
\q
```

Chỉ cần tạo database trống. Flyway sẽ tạo bảng khi backend khởi động; không cần chạy thủ công các file migration.

## 3. Chuẩn bị mã nguồn và cấu hình

## 4. Chạy backend

### Windows / PowerShell

Trong cùng một terminal, tạo khóa JWT ngẫu nhiên 32 byte, đặt thời hạn token rồi chạy ứng dụng:

```powershell
$key = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($key)
$rng.Dispose()
$env:JWT_SECRET = [Convert]::ToBase64String($key)
$env:JWT_EXPIRATION = '3600000'

.\mvnw.cmd spring-boot:run
```

### macOS / Linux

Sau khi cài JDK, PostgreSQL và có lệnh `openssl`, thực hiện tại thư mục dự án:

```bash
export JWT_SECRET="$(openssl rand -base64 32)"
export JWT_EXPIRATION=3600000

sh ./mvnw spring-boot:run
```

Các biến môi trường trên chỉ áp dụng cho phiên terminal hiện tại. Khi mở terminal mới để chạy ứng dụng hoặc test, cần khai báo lại.

Khi khởi động thành công:

- Log có dòng `Started WalletcoreApplication`.
- Backend lắng nghe tại `http://localhost:8080`.
- Flyway chạy `V1__init_schema.sql` và `V2__add_role_id_to_users.sql` trong `src/main/resources/db/migration`.
- Database có các bảng `users`, `roles`, `wallets` và bảng lịch sử `flyway_schema_history`. Bảng trung gian `user_roles` được tạo ở V1 rồi xóa ở V2.
- Hibernate đang dùng `ddl-auto: validate`: kiểm tra schema do Flyway tạo.

Dừng backend bằng `Ctrl+C`.