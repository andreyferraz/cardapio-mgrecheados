package com.mgrecheados.cardapio.controller;

import java.util.UUID;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mgrecheados.cardapio.dto.ProdutoAdminForm;
import com.mgrecheados.cardapio.model.Produto;
import com.mgrecheados.cardapio.service.ProdutoService;

@Controller
@RequestMapping("/admin")
public class AdminProdutoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminProdutoController.class);

    private static final String REDIRECT_DASHBOARD = "redirect:/admin/dashboard";
    private static final String SUCCESS_MESSAGE = "successMessage";
    private static final String ERROR_MESSAGE = "errorMessage";

    private final ProdutoService produtoService;

    public AdminProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("produtos", produtoService.listarTodos());
        return "admin/dashboard";
    }

    @GetMapping("/produtos/novo")
    public String novoProduto(Model model) {
        ProdutoAdminForm produtoForm = new ProdutoAdminForm();
        produtoForm.setAtivo(true);
        model.addAttribute("produtoForm", produtoForm);
        model.addAttribute("isEdicao", false);
        return "admin/produto-form";
    }

    @PostMapping("/produtos")
    public String cadastrarProduto(@ModelAttribute ProdutoAdminForm produtoForm,
                                   @RequestParam("imagemFile") MultipartFile imagemFile,
                                   RedirectAttributes redirectAttributes) {
        try {
            Produto produto = produtoForm.toProduto();
            produtoService.cadastrar(produto, imagemFile);
            redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE, "Produto cadastrado com sucesso.");
        } catch (RuntimeException ex) {
            logErro("Erro ao cadastrar produto", ex, produtoForm::getNome);
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE, ex.getMessage());
            return "redirect:/admin/produtos/novo";
        }
        return REDIRECT_DASHBOARD;
    }

    @GetMapping("/produtos/{id}/editar")
    public String editarProduto(@PathVariable UUID id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Produto produto = produtoService.buscarPorId(id);
            model.addAttribute("produto", produto);
            model.addAttribute("produtoForm", ProdutoAdminForm.fromProduto(produto));
            model.addAttribute("isEdicao", true);
            return "admin/produto-form";
        } catch (RuntimeException ex) {
            LOGGER.error("Erro ao abrir tela de edicao do produto {}", id, ex);
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE, ex.getMessage());
            return REDIRECT_DASHBOARD;
        }
    }

    @PostMapping("/produtos/{id}/editar")
    public String atualizarProduto(@PathVariable UUID id,
                                   @ModelAttribute ProdutoAdminForm produtoForm,
                                   @RequestParam(name = "imagemFile", required = false) MultipartFile imagemFile,
                                   RedirectAttributes redirectAttributes) {
        try {
            Produto produto = produtoForm.toProduto();
            produtoService.atualizar(id, produto, imagemFile);
            redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE, "Produto atualizado com sucesso.");
            return REDIRECT_DASHBOARD;
        } catch (RuntimeException ex) {
            logErro("Erro ao atualizar produto", ex, () -> String.valueOf(id));
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE, ex.getMessage());
            return "redirect:/admin/produtos/" + id + "/editar";
        }
    }

    @PostMapping("/produtos/{id}/excluir")
    public String excluirProduto(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            produtoService.remover(id);
            redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE, "Produto removido com sucesso.");
        } catch (RuntimeException ex) {
            LOGGER.error("Erro ao excluir produto {}", id, ex);
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE, ex.getMessage());
        }
        return REDIRECT_DASHBOARD;
    }

    private void logErro(String mensagem, RuntimeException ex, Supplier<String> contexto) {
        LOGGER.error("{} [{}]", mensagem, contexto.get(), ex);
    }
}
