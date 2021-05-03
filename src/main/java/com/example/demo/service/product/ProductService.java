package com.example.demo.service.product;

import com.example.demo.model.Product;
import com.example.demo.repo.IProductRepo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ProductService implements IProductService {
    @Autowired
    IProductRepo productRepo;

    @Override
    public Iterable<Product> findAll() {
        clawlerData();
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

    private void clawlerData(){
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
            productRepo.save(p);
        }
//        return productList;
    }
}