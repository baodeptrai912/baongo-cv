package com.example.baoNgoCv.service.utilityService;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class SendGridEmailClient {

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Value("${app.mail.from.address}")
    private String fromAddress; // "BaoNgoCV <noreply@baongocv.info>"

    public void sendHtmlEmail(String to, String subject, String htmlContent) throws IOException {
        // Nếu fromAddress có dạng "Name <email>", tách lấy email
        String fromEmail = fromAddress;
        String fromName = null;
        if (fromAddress.contains("<") && fromAddress.contains(">")) {
            int start = fromAddress.indexOf('<');
            int end = fromAddress.indexOf('>');
            fromName = fromAddress.substring(0, start).trim();
            fromEmail = fromAddress.substring(start + 1, end).trim();
        }

        Email from = new Email(fromEmail, fromName);
        Email toEmail = new Email(to);
        Content content = new Content("text/html", htmlContent);
        Mail mail = new Mail(from, subject, toEmail, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        Response response = sg.api(request);
        log.info("SendGrid response: status={}, body={}, headers={}",
                response.getStatusCode(), response.getBody(), response.getHeaders());
    }
}
