package com.example.restaurant_food_system.utils;

import org.springframework.stereotype.Component;

@Component
public class EmailContent {

    /**
     * Nội dung email cấp tài khoản
     */
    public String buildAccountEmailContent(
            String fullName,
            String email,
            String password
    ) {

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body {
                    font-family: Arial, sans-serif;
                    background-color: #f5f5f5;
                    margin: 0;
                    padding: 20px;
                }

                .container {
                    max-width: 600px;
                    margin: 0 auto;
                    background-color: #ffffff;
                    padding: 30px;
                    border-radius: 8px;
                    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
                }

                .title {
                    color: #2c3e50;
                    margin-bottom: 20px;
                }

                .info-box {
                    background-color: #f4f4f4;
                    padding: 15px;
                    border-radius: 5px;
                    margin: 20px 0;
                }

                .label {
                    font-weight: bold;
                    color: #333333;
                }

                .password {
                    color: #e74c3c;
                    font-size: 18px;
                    font-weight: bold;
                }

                .btn {
                    background-color: #4CAF50;
                    color: white !important;
                    padding: 12px 24px;
                    text-decoration: none;
                    border-radius: 5px;
                    display: inline-block;
                    margin-top: 20px;
                }

                .warning {
                    color: #e67e22;
                    font-size: 13px;
                    margin-top: 25px;
                    line-height: 1.6;
                }

                .footer {
                    margin-top: 30px;
                    font-size: 12px;
                    color: #777777;
                }
            </style>
        </head>

        <body>
            <div class="container">

                <h2 class="title">
                    Tài khoản đăng nhập hệ thống
                </h2>

                <p>Xin chào <strong>%s</strong>,</p>

                <p>
                    Quản trị viên đã tạo tài khoản cho bạn trên hệ thống quản lý nhà hàng.
                </p>

                <div class="info-box">
                    <p>
                        <span class="label">Email đăng nhập:</span>
                        %s
                    </p>

                    <p>
                        <span class="label">Mật khẩu tạm thời:</span>
                        <span class="password">%s</span>
                    </p>
                </div>

                <div class="warning">
                    <strong>Lưu ý bảo mật:</strong>
                    <ul>
                        <li>Đây là mật khẩu tạm thời được hệ thống cấp tự động.</li>
                        <li>Bạn nên đổi mật khẩu ngay sau lần đăng nhập đầu tiên.</li>
                        <li>Không chia sẻ thông tin đăng nhập cho người khác.</li>
                    </ul>
                </div>

                <div class="footer">
                    Email này được gửi tự động từ hệ thống.
                </div>

            </div>
        </body>
        </html>
    """.formatted(fullName, email, password);
    }

    /**
     * Nội dung email xác thực địa chỉ email
     */
    public String buildVerifyEmailContent(String fullName, String verifyLink) {
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body {
                    font-family: Arial, Helvetica, sans-serif;
                    background-color: #f5f5f5;
                    margin: 0;
                    padding: 20px;
                }

                .container {
                    max-width: 600px;
                    margin: 0 auto;
                    background-color: #ffffff;
                    padding: 30px;
                    border-radius: 8px;
                    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
                }

                .brand {
                    color: #4CAF50;
                    font-size: 13px;
                    font-weight: bold;
                    letter-spacing: 1px;
                    text-transform: uppercase;
                    margin-bottom: 12px;
                }

                .title {
                    color: #2c3e50;
                    margin-top: 0;
                    margin-bottom: 20px;
                }

                p {
                    color: #444444;
                    line-height: 1.6;
                }

                .btn {
                    background-color: #4CAF50;
                    color: white !important;
                    padding: 12px 24px;
                    text-decoration: none;
                    border-radius: 5px;
                    display: inline-block;
                    margin-top: 20px;
                    font-weight: bold;
                }

                .link-box {
                    background-color: #f4f4f4;
                    padding: 12px;
                    border-radius: 5px;
                    margin: 20px 0;
                    color: #2c3e50;
                    font-size: 13px;
                    word-break: break-all;
                }

                .warning {
                    color: #8a5a00;
                    font-size: 13px;
                    margin-top: 25px;
                    line-height: 1.6;
                    background-color: #fff8e1;
                    border-left: 4px solid #e67e22;
                    padding: 12px 15px;
                    border-radius: 4px;
                }

                .warning ul {
                    margin: 8px 0 0 0;
                    padding-left: 20px;
                }

                .footer {
                    margin-top: 30px;
                    font-size: 12px;
                    color: #777777;
                    border-top: 1px solid #eeeeee;
                    padding-top: 15px;
                }
            </style>
        </head>

        <body>
            <div class="container">

                <div class="brand">Hệ thống quản lý nhà hàng</div>

                <h2 class="title">Xác thực địa chỉ email</h2>

                <p>Xin chào <strong>%s</strong>,</p>

                <p>
                    Cảm ơn bạn đã đăng ký tài khoản khách hàng tại hệ thống quản lý nhà hàng.
                    Chỉ còn một bước nữa để hoàn tất đăng ký.
                </p>

                <p>
                    Vui lòng nhấn vào nút bên dưới để xác thực email và kích hoạt tài khoản của bạn:
                </p>

                <p>
                    <a href="%s" class="btn">Xác thực email</a>
                </p>

                <p>Hoặc copy liên kết sau vào trình duyệt:</p>

                <div class="link-box">%s</div>

                <div class="warning">
                    <strong>Lưu ý:</strong>
                    <ul>
                        <li>Liên kết này chỉ sử dụng được một lần.</li>
                        <li>Liên kết sẽ hết hạn sau 24 giờ.</li>
                        <li>Nếu bạn không thực hiện đăng ký, vui lòng bỏ qua email này.</li>
                    </ul>
                </div>

                <div class="footer">
                    Email này được gửi tự động từ hệ thống quản lý nhà hàng.
                    Vui lòng không trả lời trực tiếp email này.
                </div>

            </div>
        </body>
        </html>
    """.formatted(fullName, verifyLink, verifyLink);
    }
}
