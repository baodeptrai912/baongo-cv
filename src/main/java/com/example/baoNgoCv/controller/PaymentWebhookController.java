package com.example.baoNgoCv.controller;

import com.example.baoNgoCv.service.securityService.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper; // Đảm bảo import đúng
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/webhook")
public class PaymentWebhookController {

    private final PaymentService paymentService;
    // Lấy Checksum Key từ file application.properties
    @Value("${PAYOS_CHECKSUM_KEY}")
    private String checksumKey;

    // Dùng ObjectMapper "xịn" đã được cấu hình từ JacksonConfig
    private final ObjectMapper objectMapper;

    // Constructor injection
    public PaymentWebhookController(ObjectMapper objectMapper, PaymentService paymentService) {
        this.objectMapper = objectMapper;
        this.paymentService = paymentService;
    }

    /**
     * Hàm này thay thế cho payOS.webhookHandler().verify()
     * để tự xác thực chữ ký mà không bị lỗi OffsetDateTime
     */
    @PostMapping("/payos")
    public ResponseEntity<Void> handlePayOSWebhook(@RequestBody Map<String, Object> webhookBody) {

        try {
            // 1. Tách data và signature ra
            Map<String, Object> data = (Map<String, Object>) webhookBody.get("data");
            String signature = (String) webhookBody.get("signature");

            if (data == null || signature == null) {
                System.out.println("Webhook bị thiếu data hoặc signature.");
                return ResponseEntity.badRequest().build();
            }

            // 2. Sắp xếp các key của 'data' theo thứ tự alphabet
            String sortedDataString = data.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey(Comparator.naturalOrder()))
                    .map(entry -> entry.getKey() + "=" + entry.getValue().toString())
                    .collect(Collectors.joining("&"));

            // 3. Tạo chữ ký HMAC
            String mySignature = hmacSha256(sortedDataString, checksumKey);

            // 4. So sánh chữ ký của bạn với chữ ký của PayOS
            if (!mySignature.equals(signature)) {
                System.err.println("!!! CẢNH BÁO: Chữ ký Webhook KHÔNG HỢP LỆ !!!");
                return ResponseEntity.status(401).build(); // 401 Unauthorized
            }

            // --- XÁC THỰC THÀNH CÔNG ---
            // 5. Lấy orderCode (chúng ta không đụng đến transactionDateTime)
            // Lưu ý: PayOS trả về orderCode dưới dạng số, nên cần convert
            Integer orderCodeInt = (Integer) data.get("orderCode");
            String orderCode = (orderCodeInt != null) ? orderCodeInt.toString() : null;

            if (orderCode == null) {
                System.err.println("Webhook hợp lệ nhưng thiếu orderCode.");
                return ResponseEntity.badRequest().build();
            }

            System.out.println("Webhook xác thực thành công cho orderCode: " + orderCode);


            paymentService.handleSuccessfulPayment(Long.valueOf(orderCode));
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            System.err.println("Xử lý Webhook thất bại. Lỗi: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Hàm tiện ích để tạo chữ ký HMAC-SHA256
     */
    private String hmacSha256(String data, String secret) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            byte[] hashBytes = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // Convert byte array to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Không thể tạo chữ ký HMAC", e);
        }
    }
}