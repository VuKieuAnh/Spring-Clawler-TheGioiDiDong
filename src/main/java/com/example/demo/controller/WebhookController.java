package com.example.demo.controller;

import com.example.demo.model.Product;
import com.example.demo.model.User;
import com.example.demo.repo.IProductRepo;
import com.example.demo.service.MailService;
import com.example.demo.service.user.IUserService;
import com.github.messenger4j.Messenger;
import com.github.messenger4j.exception.MessengerApiException;
import com.github.messenger4j.exception.MessengerIOException;
import com.github.messenger4j.exception.MessengerVerificationException;
import com.github.messenger4j.send.MessagePayload;
import com.github.messenger4j.send.MessagingType;
import com.github.messenger4j.send.NotificationType;
import com.github.messenger4j.send.message.TextMessage;
import com.github.messenger4j.send.recipient.IdRecipient;
import com.github.messenger4j.webhook.event.TextMessageEvent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;

import static com.github.messenger4j.Messenger.*;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.apache.tomcat.util.file.ConfigFileLoader.getInputStream;

@RestController
@CrossOrigin("*")
@RequestMapping("/webhook")
public class WebhookController {

    @Autowired
    IProductRepo productRepo;

    @Autowired
    IUserService userService;

    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);

    private final Messenger messenger;

    @Autowired
    public WebhookController(final Messenger messenger) {
        this.messenger = messenger;
    }

    @GetMapping
    public ResponseEntity<String> verifyWebhook(@RequestParam(MODE_REQUEST_PARAM_NAME) final String mode,
                                                @RequestParam(VERIFY_TOKEN_REQUEST_PARAM_NAME) final String verifyToken, @RequestParam(CHALLENGE_REQUEST_PARAM_NAME) final String challenge) {
        logger.debug("Received Webhook verification request - mode: {} | verifyToken: {} | challenge: {}", mode, verifyToken, challenge);
        try {
            this.messenger.verifyWebhook(mode, verifyToken);
            return ResponseEntity.ok(challenge);
        } catch (MessengerVerificationException e) {
            logger.warn("Webhook verification failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<Void> handleCallback(@RequestBody final String payload, @RequestHeader(SIGNATURE_HEADER_NAME) final String signature) throws MessengerVerificationException {
        this.messenger.onReceiveEvents(payload, of(signature), event -> {
            if (event.isTextMessageEvent()) {
                try {
                    logger.info("0");
                    handleTextMessageEvent(event.asTextMessageEvent());
                    logger.info("1");
                } catch (MessengerApiException e) {
                    logger.info("2");
                    e.printStackTrace();
                } catch (MessengerIOException e) {
                    logger.info("3");
                    e.printStackTrace();
                }
            } else {
                String senderId = event.senderId();
                sendTextMessageUser(senderId, "Tôi là bot chỉ có thể xử lý tin nhắn văn bản.");
            }
        });
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    private void handleTextMessageEvent(TextMessageEvent event) throws MessengerApiException, MessengerIOException {
        final String senderId = event.senderId();
        Long id = Long.parseLong(senderId);
        Optional<User> userOptional = userService.findById(id);
        if(userOptional.isPresent()){
            sendTextMessageUser(senderId, "Xin chào! Đây là ung dung gui thong tin");
        }else {
            User user = new User();
            user.setId(id);
            userService.save(user);
        }

    }
    @Scheduled(cron = "0 */2 * * * *")
    private void clawlerData(){
        List<User> users = (List<User>) userService.findAll();
        if(!users.isEmpty()){
            String urlRoot = "https://www.thegioididong.com";
            Document doc = null;
            try {
                doc = Jsoup.connect("https://www.thegioididong.com/may-doi-tra/laptop-dell?o=gia-thap-den-cao").data("query", "Java").userAgent("Chrome").cookie("auth", "token").timeout(5000).post();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Elements elements = doc.getElementById("lstModel").children();
            String a = elements.toString();
            a = a.replaceAll("\\R", "");
            List<Product> productList = new ArrayList<>();
//        System.out.println(a);
            // Regex tên sp
            Pattern p1 = Pattern.compile("<h3>(.*?)</h3>");
//        Pattern p1 = Pattern.compile("<div class=\"fname\">(.*?)</div>");
            Matcher m1 = p1.matcher(a);
//        System.out.println("ten sp");
//        int count = 0;
//        while (m1.find()) {
////            count++;
//            System.out.println(m1.group(1).trim());
//        }
            // link sp
            Pattern p2 = Pattern.compile("href=\"(.*?)\"");
//        Pattern p1 = Pattern.compile("<div class=\"fname\">(.*?)</div>");
            Matcher m2 = p2.matcher(a);
//        System.out.println("link sp");
//        while (m2.find()) {
//            System.out.println(urlRoot+m2.group(1).trim());
//        }
            // số lương  sp
            Pattern p3 = Pattern.compile("<span class=\"quantity\">(.*?)</span>");
//        Pattern p1 = Pattern.compile("<div class=\"fname\">(.*?)</div>");
            Matcher m3 = p3.matcher(a);
//        System.out.println("số lượng sp");
            while (m1.find()&& m2.find()&& m3.find()) {
                String name = m1.group(1).trim();
                String url = urlRoot + m2.group(1).trim();
                String number =  m3.group(1).trim();
                Product p = new Product(name, url, number);
                productList.add(p);
                for (User user: users){
                    sendTextMessageUser(user.getId().toString(), p.getName());
                }
//            id Doan Tai PC = "2370104899971095"
//            sendTextMessageUser("2370104899971095",p.getName());
                if(check(p)){
                    productRepo.save(p);

                    //gửi mail
//                sendEmail(p);
//                sendTextMessageUser("1107234773074397",p.getName());

                }
//            productRepo.save(p);
            }
        }
    }

    public boolean check(Product product){
        List<Product> products1 = (List<Product>) productRepo.findAll();
        for (Product p: products1){
            if(product.getName().equals(p.getName())){
                return false;
            }
        }
        return true;
    }

    private void sendTextMessageUser(String idSender, String text) {
        try {
            final IdRecipient recipient = IdRecipient.create(idSender);
            final NotificationType notificationType = NotificationType.REGULAR;
            final String metadata = "DEVELOPER_DEFINED_METADATA";

            final TextMessage textMessage = TextMessage.create(text, empty(), of(metadata));
            final MessagePayload messagePayload = MessagePayload.create(recipient, MessagingType.RESPONSE, textMessage,
                    of(notificationType), empty());
            this.messenger.send(messagePayload);
        } catch (MessengerApiException | MessengerIOException e) {
            handleSendException(e);
        }
    }

    private void handleSendException(Exception e) {
        logger.error("Message could not be sent. An unexpected error occurred.", e);
    }
}
