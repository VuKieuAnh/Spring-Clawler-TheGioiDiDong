package com.example.demo.service.product;

import com.example.demo.model.Mail;
import com.example.demo.model.Product;
import com.example.demo.repo.IProductRepo;
import com.example.demo.service.MailService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ProductService implements IProductService {
    private List<Product> products;
//    private static List<Product> products1 = productRepo.findAll();
    @Autowired
    IProductRepo productRepo;

    @Autowired
    private MailService mailService;

    @Override
    public Iterable<Product> findAll() {
//        clawlerData();
        return productRepo.findAll();
    }

    @Override
    public Optional<Product> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public Product save(Product product) {
        return null;
    }

    @Override
    public void remove(Long id) {

    }

    @Scheduled(cron = "0 */1 * * * *")
    private List<Product> clawlerData(){
        products = new ArrayList<>();
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
//        List<Product> productList = new ArrayList<>();
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
            products.add(p);
            if(check(p)){
                long millis = System.currentTimeMillis();
                Date date = new Date(millis);
                p.setDate(date);
                productRepo.save(p);
                //gửi mail
                sendEmail(p);

            }
//            productRepo.save(p);
        }
        List<Product> productsInDB = (List<Product>) productRepo.findAll();
        for (Product p:productsInDB
             ) {
            boolean check = false;
            for (Product pInListClawler: products
                 ) {
                if (p.getName().equals(pInListClawler.getName())) check = true;
            }
            if (!check){
                productRepo.delete(p);
            }
        }

        return products;
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

    public void sendEmail(Product product){
        Mail mail = new Mail();
        mail.setMailFrom("vukieuanh.hnue@gmail.com");
        mail.setMailTo("phucit.mediahn@gmail.com");
//        mail.setMailTo("vukieuanh.hnue@gmail.com");
//        mail.setMailCc("vukieuanh.hnue@gmail.com");
        mail.setMailSubject("Email change the gioi di dong");
        mail.setMailContent("Có 1 sản phẩm " + product.getName() + " mới được cập nhật " + product.getUrl() + " số lượng " + product.getNumber());

        mailService.sendEmail(mail);
    }
}