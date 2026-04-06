package com.mgrecheados.cardapio.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.mgrecheados.cardapio.service.ProdutoService;

@Controller
public class CardapioController {

    private final ProdutoService produtoService;

    @Value("${app.whatsapp.number}")
    private String whatsappNumber;

    @Value("${app.whatsapp.message-prefix:Ola, gostaria de fazer o pedido:}")
    private String whatsappMessagePrefix;

    public CardapioController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @GetMapping({"/", "/cardapio"})
    public String cardapio(Model model) {
        model.addAttribute("produtos", produtoService.listarAtivos());
        model.addAttribute("whatsappNumber", whatsappNumber);
        model.addAttribute("whatsappMessagePrefix", whatsappMessagePrefix);
        return "cardapio";
    }
}
