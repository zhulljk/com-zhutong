package com.example.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.example.service.VerificationDeliveryService;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class VerificationDeliveryServiceImpl implements VerificationDeliveryService {
    private static final String ALIYUN_HOST = "dysmsapi.aliyuncs.com";
    @Value("${sms.mail.host:}")
    private String mailHost;
    @Value("${sms.mail.port:0}")
    private int mailPort;
    @Value("${sms.mail.username:}")
    private String mailUsername;
    @Value("${sms.mail.password:}")
    private String mailPassword;
    @Value("${sms.mail.from:}")
    private String mailFrom;
    @Value("${sms.aliyun.accessKeyId:}")
    private String aliyunAccessKeyId;
    @Value("${sms.aliyun.accessKeySecret:}")
    private String aliyunAccessKeySecret;
    @Value("${sms.aliyun.signName:}")
    private String aliyunSignName;
    @Value("${sms.aliyun.templateCode:}")
    private String aliyunTemplateCode;

    @Override
    public DeliveryResult deliver(String pushType, String medium, String loginType, String code, long ttlSeconds) {
        String normalized = normalize(pushType);
        try {
            if ("email".equals(normalized) || "mail".equals(normalized)) {
                sendEmail(medium, loginType, code, ttlSeconds);
                return new DeliveryResult(true, "邮件已发送到" + medium);
            }
            if ("sms".equals(normalized) || "aliyun".equals(normalized)) {
                String message = sendAliyunSms(medium, loginType, code, ttlSeconds);
                return new DeliveryResult(true, message);
            }
        } catch (Exception e) {
            return new DeliveryResult(false, e.getMessage());
        }
        return new DeliveryResult(false, "不支持的推送类型");
    }

    private void sendEmail(String to, String loginType, String code, long ttlSeconds) throws Exception {
        if (!StringUtils.hasText(mailHost)
                || !StringUtils.hasText(mailUsername)
                || !StringUtils.hasText(mailPassword)
                || !StringUtils.hasText(mailFrom)) {
            throw new IllegalStateException("mail config is missing");
        }
        int port = mailPort > 0 ? mailPort : 465;
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.host", mailHost);
        props.put("mail.smtp.port", String.valueOf(port));
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mailUsername, mailPassword);
            }
        });
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(mailFrom));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(buildEmailSubject(loginType));
        message.setText(buildEmailContent(loginType, code, ttlSeconds));
        Transport.send(message);
    }

    private String sendAliyunSms(String phone, String loginType, String code, long ttlSeconds) throws Exception {
        if (!StringUtils.hasText(aliyunAccessKeyId)
                || !StringUtils.hasText(aliyunAccessKeySecret)
                || !StringUtils.hasText(aliyunSignName)
                || !StringUtils.hasText(aliyunTemplateCode)) {
            throw new IllegalStateException("aliyun sms config is missing");
        }
        Map<String, String> params = new HashMap<>();
        params.put("Format", "JSON");
        params.put("Version", "2017-05-25");
        params.put("AccessKeyId", aliyunAccessKeyId);
        params.put("SignatureMethod", "HMAC-SHA1");
        params.put("Timestamp", getTimestamp());
        params.put("SignatureVersion", "1.0");
        params.put("SignatureNonce", UUID.randomUUID().toString());
        params.put("RegionId", "cn-hangzhou");
        params.put("Action", "SendSms");
        params.put("PhoneNumbers", phone);
        params.put("SignName", aliyunSignName);
        params.put("TemplateCode", aliyunTemplateCode);
        Map<String, String> templateParams = new HashMap<>();
        templateParams.put("code", code);
        templateParams.put("loginType", loginType);
        templateParams.put("expireMinutes", String.valueOf(getExpireMinutes(ttlSeconds)));
        params.put("TemplateParam", JSON.toJSONString(templateParams));
        String signature = signAliyun(params, aliyunAccessKeySecret);
        params.put("Signature", signature);
        StringBuilder query = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            query.append("&")
                    .append(URLEncoder.encode(entry.getKey(), "UTF-8"))
                    .append("=")
                    .append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        String url = "https://" + ALIYUN_HOST + "/?" + query.substring(1);
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(url);
            try (CloseableHttpResponse response = httpClient.execute(post)) {
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                JSONObject json = JSON.parseObject(result);
                String respCode = json.getString("Code");
                if ("OK".equals(respCode)) {
                    return "短信已发送到" + phone;
                }
                String message = json.getString("Message");
                throw new IllegalStateException(message != null ? message : "短信发送失败");
            }
        }
    }

    private String signAliyun(Map<String, String> params, String accessKeySecret) throws Exception {
        Map<String, String> sorted = new java.util.TreeMap<>(params);
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            sb.append("&")
                    .append(URLEncoder.encode(entry.getKey(), "UTF-8"))
                    .append("=")
                    .append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        String stringToSign = "GET&%2F&" + URLEncoder.encode(sb.substring(1), "UTF-8");
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec((accessKeySecret + "&").getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
        byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(signData);
    }

    private String getTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(new Date());
    }

    private int getExpireMinutes(long ttlSeconds) {
        long minutes = ttlSeconds / 60;
        return minutes > 0 ? (int) minutes : 1;
    }

    private String buildEmailSubject(String loginType) {
        return "验证码通知";
    }

    private String buildEmailContent(String loginType, String code, long ttlSeconds) {
        return "【账号验证】登录方式:" + loginType + "，验证码:" + code + "，" + getExpireMinutes(ttlSeconds) + "分钟内有效。";
    }

    private String normalize(String value) {
        return value.trim().toLowerCase();
    }
}
