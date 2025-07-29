package dev.wakandaacademy.produdoro.usuario.application.service;

import dev.wakandaacademy.produdoro.DataHelper;
import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.StatusUsuario;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class UsuarioApplicationServiceTest {
    @InjectMocks
    UsuarioApplicationService usuarioApplicationService;

    @Mock
    UsuarioRepository usuarioRepository;

    private Usuario usuarioMock;
    private  final String usuarioEmail = "usuario@teste.com";
    private  final UUID idUsuario = UUID.randomUUID();

    @BeforeEach
    void setup(){
        MockitoAnnotations.openMocks(this);
        usuarioMock = mock(Usuario.class);
        when(usuarioRepository.buscaUsuarioPorEmail(usuarioEmail)).thenReturn(usuarioMock);
    }



    @Test
    void deveMudarStatusParaFoco() {
        //dado
        Usuario usuario = DataHelper.createUsuario();
        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(usuarioRepository.salva(usuario)).thenReturn(usuario);

        //quando
        usuarioApplicationService.mudaStatusParaFoco(usuario.getEmail(), usuario.getIdUsuario());

        //então
        assertEquals(StatusUsuario.FOCO, usuario.getStatus());
        verify(usuarioRepository, times(1)).buscaUsuarioPorEmail(usuario.getEmail());
        verify(usuarioRepository, times(1)).salva(usuario);
    }

    @Test
    void naoDeveMudarParaFocoQuandoUsuarioNaoEhValido() {
        Usuario usuario = DataHelper.createUsuario();
        UUID idInexistente = UUID.randomUUID();
        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);

        APIException exception = assertThrows(APIException.class, () -> usuarioApplicationService.mudaStatusParaFoco(usuario.getEmail(), idInexistente));

        assertEquals(StatusUsuario.PAUSA_LONGA, usuario.getStatus());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusException());
        assertEquals("credencial de autenticação não é valida", exception.getMessage());
        verify(usuarioRepository, times(1)).buscaUsuarioPorEmail(usuario.getEmail());
        verify(usuarioRepository, never()).salva(usuario);
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioJaEstaEmFoco() {
        Usuario usuario = DataHelper.createUsuarioFoco();
        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);

        APIException exception = assertThrows(APIException.class, () -> usuarioApplicationService.mudaStatusParaFoco(usuario.getEmail(), usuario.getIdUsuario()));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusException());
        assertEquals("Usuário já está em FOCO", exception.getMessage());
        verify(usuarioRepository, times(1)).buscaUsuarioPorEmail(usuario.getEmail());
        verify(usuarioRepository, never()).salva(usuario);
    }

    @Test
    void deveMudarStatusParaPausaCurta() {
        Usuario usuario = DataHelper.createUsuario();
        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(usuarioRepository.salva(usuario)).thenReturn(usuario);

        usuarioApplicationService.mudaStatusParaPausaCurta(usuario.getEmail(), usuario.getIdUsuario());

        assertEquals(StatusUsuario.PAUSA_CURTA, usuario.getStatus());
        verify(usuarioRepository, times(1)).buscaUsuarioPorEmail(usuario.getEmail());
        verify(usuarioRepository, times(1)).salva(usuario);
    }

    @Test
    void naoDevePermitirPausaCurtaSeTokenInvalido() {
        Usuario usuario = DataHelper.createUsuario();
        UUID outroUsuarioId = UUID.randomUUID();
        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);

        APIException exception = assertThrows(APIException.class, () -> usuarioApplicationService.mudaStatusParaPausaCurta(usuario.getEmail(), outroUsuarioId));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusException());
        assertEquals("credencial de autenticação não é valida", exception.getMessage());
        verify(usuarioRepository, times(1)).buscaUsuarioPorEmail(usuario.getEmail());
        verify(usuarioRepository, never()).salva(usuario);
        assertEquals(StatusUsuario.PAUSA_LONGA, usuario.getStatus());
    }

    @Test
    void naoPermitirPausaCurtaSeStatusJaEstaEmPausaCurta() {
        Usuario usuario = DataHelper.createUsuario().toBuilder()
                .status(StatusUsuario.PAUSA_CURTA)
                .build();

        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);

        APIException exception = assertThrows(APIException.class, () -> usuarioApplicationService.mudaStatusParaPausaCurta(usuario.getEmail(), usuario.getIdUsuario()));
        assertEquals(HttpStatus.CONFLICT, exception.getStatusException());
        assertEquals("Usuário já está em PAUSA CURTA", exception.getMessage());
        verify(usuarioRepository, times(1)).buscaUsuarioPorEmail(usuario.getEmail());
        verify(usuarioRepository, never()).salva(usuario);
        assertEquals(StatusUsuario.PAUSA_CURTA, usuario.getStatus());
    }
    @Test
    void deveMudarStatusParaPausaLonga(){
        Usuario usuario = DataHelper.createUsuarioFoco();
        when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);
        when(usuarioRepository.buscaUsuarioPorId(any())).thenReturn(usuario);
        usuarioApplicationService.mudaStatusParaPausaLonga(usuario.getEmail(), usuario.getIdUsuario());

        verify(usuarioRepository, times(1)).salva(usuario);
        assertEquals(StatusUsuario.PAUSA_LONGA, usuario.getStatus());
    }

    @Test
    void mudarStatusParaPausaLonga_DeveLancarExcecaoUsuarioJaEstaEmPausaLonga(){
        Usuario usuario = DataHelper.createUsuario();
        UUID idUsuario = usuario.getIdUsuario();
        when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);
        when(usuarioRepository.buscaUsuarioPorId(any())).thenReturn(usuario);

        APIException exception = assertThrows(APIException.class,
                () -> usuarioApplicationService.mudaStatusParaPausaLonga("usuario@teste.com", idUsuario));

        assertEquals("Usuário já está em pausa longa", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatusException());
        verify(usuarioRepository, times(1)).buscaUsuarioPorEmail("usuario@teste.com");
    }
}