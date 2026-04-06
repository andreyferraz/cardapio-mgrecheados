package com.mgrecheados.cardapio.service;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.context.event.EventListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mgrecheados.cardapio.model.Usuario;
import com.mgrecheados.cardapio.repository.UsuarioRepository;
import com.mgrecheados.cardapio.utils.ValidationUtils;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import org.springframework.boot.context.event.ApplicationReadyEvent;

@Service
public class UsuarioService implements UserDetailsService {

    private static final String USUARIO_CAMPO = "Usuário";
    private static final String ID_USUARIO_CAMPO = "Id do usuário";
    private static final String USERNAME_CAMPO = "Username";
    private static final String SENHA_CAMPO = "Senha";
    private static final String PERFIL_CAMPO = "Perfil";
    private static final Pattern BCRYPT_PATTERN = Pattern.compile("^\\$2[aby]\\$\\d{2}\\$.{53}$");

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario cadastrar(Usuario usuario) {
        validarUsuario(usuario);
        usuario.setPassword(encodeIfNeeded(usuario.getPassword()));
        usuario.setRole(normalizarRole(usuario.getRole()));
        return usuarioRepository.save(usuario);
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario buscarPorId(UUID id) {
        ValidationUtils.validarCampoObrigatorio(id, ID_USUARIO_CAMPO);
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado para o id: " + id));
    }

    public Usuario atualizar(UUID id, Usuario dadosAtualizados) {
        ValidationUtils.validarCampoObrigatorio(id, ID_USUARIO_CAMPO);
        validarUsuario(dadosAtualizados);

        Usuario usuarioExistente = buscarPorId(id);
        usuarioExistente.setUsername(dadosAtualizados.getUsername());
        usuarioExistente.setPassword(encodeIfNeeded(dadosAtualizados.getPassword()));
        usuarioExistente.setRole(normalizarRole(dadosAtualizados.getRole()));

        return usuarioRepository.save(usuarioExistente);
    }

    public void remover(UUID id) {
        ValidationUtils.validarCampoObrigatorio(id, ID_USUARIO_CAMPO);
        Usuario usuario = buscarPorId(id);
        usuarioRepository.delete(usuario);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));

        GrantedAuthority authority = new SimpleGrantedAuthority(normalizarRole(usuario.getRole()));
        return User.withUsername(usuario.getUsername())
                .password(usuario.getPassword())
                .authorities(authority)
                .build();
    }

    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void hashSenhasEmTextoPuroAoInicializar() {
        List<Usuario> usuarios = usuarioRepository.findAll();

        boolean houveAlteracao = false;
        for (Usuario usuario : usuarios) {
            validarUsuario(usuario);
            String roleNormalizada = normalizarRole(usuario.getRole());
            if (!isBcryptHash(usuario.getPassword())) {
                usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
                houveAlteracao = true;
            }
            if (!roleNormalizada.equals(usuario.getRole())) {
                usuario.setRole(roleNormalizada);
                houveAlteracao = true;
            }
        }

        if (houveAlteracao) {
            usuarioRepository.saveAll(usuarios);
        }
    }

    private void validarUsuario(Usuario usuario) {
        ValidationUtils.validarCampoObrigatorio(usuario, USUARIO_CAMPO);
        ValidationUtils.validarCampoStringObrigatorio(usuario.getUsername(), USERNAME_CAMPO);
        ValidationUtils.validarCampoStringObrigatorio(usuario.getPassword(), SENHA_CAMPO);
        ValidationUtils.validarCampoStringObrigatorio(usuario.getRole(), PERFIL_CAMPO);
    }

    private String encodeIfNeeded(String senha) {
        if (isBcryptHash(senha)) {
            return senha;
        }
        return passwordEncoder.encode(senha);
    }

    private boolean isBcryptHash(String senha) {
        return senha != null && BCRYPT_PATTERN.matcher(senha).matches();
    }

    private String normalizarRole(String role) {
        ValidationUtils.validarCampoStringObrigatorio(role, PERFIL_CAMPO);
        String roleLimpo = role.trim().toUpperCase();
        if (!roleLimpo.startsWith("ROLE_")) {
            roleLimpo = "ROLE_" + roleLimpo;
        }
        return roleLimpo;
    }

}
