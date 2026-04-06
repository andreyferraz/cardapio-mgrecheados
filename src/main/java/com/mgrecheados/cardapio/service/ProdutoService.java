package com.mgrecheados.cardapio.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mgrecheados.cardapio.model.Produto;
import com.mgrecheados.cardapio.repository.ProdutoRepository;
import com.mgrecheados.cardapio.utils.ValidationUtils;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ProdutoService {

    private static final String ID_PRODUTO_CAMPO = "Id do produto";
    private static final String PRODUTO_CAMPO = "Produto";

    private final ProdutoRepository produtoRepository;
    private final FileUploadService fileUploadService;

    public ProdutoService(ProdutoRepository produtoRepository, FileUploadService fileUploadService) {
        this.produtoRepository = produtoRepository;
        this.fileUploadService = fileUploadService;
    }

    public Produto cadastrar(Produto produto, MultipartFile imagemFile) {
        validarProduto(produto);
        validarImagemObrigatoria(imagemFile);

        String nomeArquivoImagem = fileUploadService.salvarImagem(imagemFile);
        produto.setImagemUrl(nomeArquivoImagem);
        produto.setAtivo(true);

        return produtoRepository.save(produto);
    }

    public List<Produto> listarTodos() {
        return produtoRepository.findAll();
    }

    public Produto buscarPorId(UUID id) {
        ValidationUtils.validarCampoObrigatorio(id, ID_PRODUTO_CAMPO);
        return produtoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado para o id: " + id));
    }

    public Produto atualizar(UUID id, Produto dadosAtualizados, MultipartFile novaImagemFile) {
        ValidationUtils.validarCampoObrigatorio(id, ID_PRODUTO_CAMPO);
        ValidationUtils.validarCampoObrigatorio(dadosAtualizados, PRODUTO_CAMPO);

        Produto produtoExistente = buscarPorId(id);
        atualizarCamposProduto(produtoExistente, dadosAtualizados);

        if (novaImagemFile != null && !novaImagemFile.isEmpty()) {
            substituirImagem(produtoExistente, novaImagemFile);
        } else if (produtoExistente.getImagemUrl() == null || produtoExistente.getImagemUrl().isBlank()) {
            throw new IllegalArgumentException("Produto deve possuir uma imagem cadastrada.");
        }

        return produtoRepository.save(produtoExistente);
    }

    public void remover(UUID id) {
        ValidationUtils.validarCampoObrigatorio(id, ID_PRODUTO_CAMPO);

        Produto produto = buscarPorId(id);
        if (produto.getImagemUrl() != null && !produto.getImagemUrl().isBlank()) {
            fileUploadService.removerImagem(produto.getImagemUrl());
        }
        produtoRepository.delete(produto);
    }

    private void validarProduto(Produto produto) {
        ValidationUtils.validarCampoObrigatorio(produto, PRODUTO_CAMPO);
        ValidationUtils.validarCampoStringObrigatorio(produto.getNome(), "Nome");
        ValidationUtils.validarCampoStringObrigatorio(produto.getDescricao(), "Descrição");
        ValidationUtils.validarCampoStringObrigatorio(produto.getPeso(), "Peso");
        ValidationUtils.validarCampoObrigatorio(produto.getPreco(), "Preço");
    }

    private void validarImagemObrigatoria(MultipartFile imagemFile) {
        if (imagemFile == null || imagemFile.isEmpty()) {
            throw new IllegalArgumentException("Imagem do produto é obrigatória.");
        }
    }

    private void atualizarCamposProduto(Produto produtoExistente, Produto dadosAtualizados) {
        validarProduto(dadosAtualizados);
        produtoExistente.setNome(dadosAtualizados.getNome());
        produtoExistente.setDescricao(dadosAtualizados.getDescricao());
        produtoExistente.setPeso(dadosAtualizados.getPeso());
        produtoExistente.setPreco(dadosAtualizados.getPreco());
        produtoExistente.setAtivo(dadosAtualizados.isAtivo());
    }

    private void substituirImagem(Produto produtoExistente, MultipartFile novaImagemFile) {
        String imagemAntiga = produtoExistente.getImagemUrl();
        String novaImagem = fileUploadService.salvarImagem(novaImagemFile);
        produtoExistente.setImagemUrl(novaImagem);

        if (imagemAntiga != null && !imagemAntiga.isBlank()) {
            fileUploadService.removerImagem(imagemAntiga);
        }
    }

}
