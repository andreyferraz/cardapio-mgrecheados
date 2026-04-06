package com.mgrecheados.cardapio.service;

import org.springframework.stereotype.Service;

import com.mgrecheados.cardapio.repository.ProdutoRepository;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

}
