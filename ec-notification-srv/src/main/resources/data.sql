INSERT INTO messages (
    id,
    code,
    name,
    url,
    mail_template,
    created_at,
    created_by,
    updated_at,
    updated_by
) VALUES (
             1,
             'EMAIL',
             'Notification email',
             'https://your-url.com',
             '<!DOCTYPE html>
         <html lang="vi">

         <head>
             <meta charset="UTF-8">
             <meta name="viewport" content="width=device-width, initial-scale=1.0">
             <title>Email Preview</title>
             <style>
                 .email-container {
                     margin: 0;
                     padding: 0;
                     box-sizing: border-box;
                 }

                 .email-container {
                     max-width: 600px;
                     margin: 0 auto;
                     background: #ffffff;
                     border-radius: 16px;
                     overflow: hidden;
                     box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
                 }

                 .header {
                     background: linear-gradient(135deg, #1e5ba8 0%, #2a7fd8 100%);
                     padding: 40px 30px;
                     text-align: center;
                     position: relative;
                     overflow: hidden;
                 }

                 .header::before {
                    content: '''';
                     position: absolute;
                     top: -50%;
                     right: -50%;
                     width: 200%;
                     height: 200%;
                     background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
                     animation: pulse 4s ease-in-out infinite;
                 }

                 @keyframes pulse {

                     0%,
                     100% {
                         transform: scale(1);
                         opacity: 0.5;
                     }

                     50% {
                         transform: scale(1.1);
                         opacity: 0.8;
                     }
                 }

                 .logo-section {
                     position: relative;
                     z-index: 1;
                 }

                 .mobifone-text {
                     font-size: 32px;
                     font-weight: 700;
                     color: #ffffff;
                     letter-spacing: 2px;
                     margin-bottom: 8px;
                     text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2);
                 }

                 .econtract-text {
                     font-size: 38px;
                     font-weight: 800;
                     color: #ffffff;
                     letter-spacing: 1px;
                     text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2);
                 }

                 .content {
                     padding: 40px 30px;
                 }

                 .greeting {
                     font-size: 24px;
                     color: #2c3e50;
                     margin-bottom: 30px;
                     font-weight: 600;
                 }

                 .greeting .name {
                     color: #1e5ba8;
                     font-weight: 700;
                 }

                 .status-message {
                     background: linear-gradient(135deg, #e8f4f8 0%, #f0f9fc 100%);
                     border-left: 4px solid #1e5ba8;
                     padding: 20px;
                     border-radius: 8px;
                     margin-bottom: 30px;
                 }

                 .status-message p {
                     font-size: 16px;
                     color: #34495e;
                     margin-bottom: 8px;
                 }

                 .status-message .status-text {
                     font-size: 18px;
                     color: #27ae60;
                     font-weight: 600;
                 }

                 .contract-title {
                     font-size: 26px;
                     color: #1e5ba8;
                     font-weight: 700;
                     text-align: center;
                     margin: 25px 0;
                     padding: 15px;
                     background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
                     border-radius: 8px;
                 }

                 .info-section {
                     background: #f8f9fa;
                     padding: 25px;
                     border-radius: 12px;
                     margin-bottom: 30px;
                 }

                 .info-row {
                     display: flex;
                     margin-bottom: 18px;
                     align-items: flex-start;
                 }

                 .info-row:last-child {
                     margin-bottom: 0;
                 }

                 .info-label {
                     font-weight: 600;
                     color: #2c3e50;
                     min-width: 140px;
                     font-size: 15px;
                 }

                 .info-value {
                     color: #1e5ba8;
                     font-weight: 600;
                     font-size: 15px;
                     word-break: break-word;
                 }

                 .message-section {
                     background: #fff9e6;
                     border-left: 4px solid #f39c12;
                     padding: 20px;
                     border-radius: 8px;
                     margin-bottom: 30px;
                 }

                 .message-label {
                     font-weight: 600;
                     color: #2c3e50;
                     margin-bottom: 10px;
                     font-size: 15px;
                 }

                 .message-content {
                     color: #555;
                     font-size: 15px;
                     line-height: 1.6;
                 }

                 .button-container {
                     text-align: center;
                     margin: 35px 0;
                 }

                 .cta-button {
                     display: inline-block;
                     background: linear-gradient(135deg, #1e5ba8 0%, #2a7fd8 100%);
                     color: #ffffff;
                     padding: 16px 50px;
                     text-decoration: none;
                     border-radius: 50px;
                     font-weight: 700;
                     font-size: 17px;
                     box-shadow: 0 8px 20px rgba(30, 91, 168, 0.3);
                     transition: all 0.3s ease;
                     letter-spacing: 0.5px;
                 }

                 .cta-button:hover {
                     transform: translateY(-2px);
                     box-shadow: 0 12px 30px rgba(30, 91, 168, 0.4);
                     background: linear-gradient(135deg, #1a4d8f 0%, #2468b8 100%);
                 }

                 .extract-note {
                     text-align: center;
                     color: #7f8c8d;
                     font-size: 13px;
                     font-style: italic;
                     margin-top: 20px;
                 }

                 .footer {
                     background: #2c3e50;
                     padding: 25px;
                     text-align: center;
                     color: #ecf0f1;
                     font-size: 13px;
                 }

                 .footer-links {
                     margin-top: 15px;
                 }

                 .footer-links a {
                     color: #3498db;
                     text-decoration: none;
                     margin: 0 10px;
                     transition: color 0.3s ease;
                 }

                 .footer-links a:hover {
                     color: #5dade2;
                 }

                 .divider {
                     height: 3px;
                     background: linear-gradient(90deg, transparent, #1e5ba8, transparent);
                     margin: 25px 0;
                 }

                 @media (max-width: 600px) {
                     .email-container {
                         border-radius: 0;
                     }

                     .content {
                         padding: 30px 20px;
                     }

                     .header {
                         padding: 30px 20px;
                     }

                     .mobifone-text {
                         font-size: 24px;
                     }

                     .econtract-text {
                         font-size: 28px;
                     }

                     .info-row {
                         flex-direction: column;
                     }

                     .info-label {
                         min-width: auto;
                         margin-bottom: 5px;
                     }

                     .cta-button {
                         padding: 14px 35px;
                         font-size: 15px;
                     }
                 }
             </style>
         </head>

         <body>
             <div class="email-container">
                 <div class="header">
                     <div class="logo-section">
                         <div class="econtract-text">eContract - Hợp đồng điện tử</div>
                     </div>
                 </div>

                 <div class="content">
                     <div class="greeting">
                         Xin chào/ Dear: <span class="name">{recipientName}</span>
                     </div>

                     <div class="status-message">
                         <p>{titleEmail}</p>
                     </div>

                     <div class="contract-title">
                         {contractName}
                     </div>

                     <div class="divider"></div>

                     <div class="info-section">
                         <div class="info-row">
                             <div class="info-label">Mã hợp đồng:</div>
                             <div class="info-value">{contractNo}</div>
                         </div>

                         <div class="info-row">
                             <div class="info-label">Gửi từ:</div>
                             <div class="info-value">{sendFrom}</div>
                         </div>

                     </div>

                     <div class="message-section">
                         <div class="message-label">Lời nhắn:</div>
                         <div class="message-content">
                             {note}
                         </div>
                     </div>

                     <div class="button-container">
                         <a href="{url}" class="cta-button">
                             {actionButton}
                         </a>
                     </div>
                 </div>

                 <div class="footer">
                     <p>&copy; 2025 eContract. All rights reserved.</p>
                     <div class="footer-links">
                         <a href="#">Hỗ trợ</a> |
                         <a href="#">Điều khoản</a> |
                         <a href="#">Bảo mật</a>
                     </div>
                 </div>
             </div>
         </body>

         </html>',
             NOW(),
             1,
             NOW(),
             1
         );

-- Đồng bộ lại sequence cho bảng messages
SELECT setval(pg_get_serial_sequence('messages', 'id'), COALESCE(MAX(id), 1)) FROM messages;