package com.mgrecheados.cardapio.dto;

import java.math.BigDecimal;

import com.mgrecheados.cardapio.model.Produto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProdutoAdminForm {

    private String nome;
    private String descricao;
    private String peso;
    private BigDecimal preco;
    private boolean ativo;

    public Produto toProduto() {
        Produto produto = new Produto();
        produto.setNome(nome);
        produto.setDescricao(descricao);
        produto.setPeso(peso);
        produto.setPreco(preco);
        produto.setAtivo(ativo);
        return produto;
    }

    public static ProdutoAdminForm fromProduto(Produto produto) {
        ProdutoAdminForm form = new ProdutoAdminForm();
        form.setNome(produto.getNome());
        form.setDescricao(produto.getDescricao());
        form.setPeso(produto.getPeso());
        form.setPreco(produto.getPreco());
        form.setAtivo(produto.isAtivo());
        return form;
    }
}
