package com.example.demo.controller;

import com.example.demo.model.Mail;
import com.example.demo.service.MailService;
import com.example.demo.service.product.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class EmailDemoController {

    @Autowired
    private IProductService productService;

    @GetMapping("")
    public ModelAndView home(){

        ModelAndView modelAndView = new ModelAndView("home");
        modelAndView.addObject("list", productService.findAll());
        return modelAndView;
    }
}
