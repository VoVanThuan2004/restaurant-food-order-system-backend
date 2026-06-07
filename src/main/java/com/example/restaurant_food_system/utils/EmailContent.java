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
}
