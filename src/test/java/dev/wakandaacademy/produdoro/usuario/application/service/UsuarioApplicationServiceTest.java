package dev.wakandaacademy.produdoro.usuario.application.service;

import dev.wakandaacademy.produdoro.DataHelper;
import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.StatusUsuario;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

    @Test
    void mudarStatusParaPausaLonga_DeveLancarExcecaoUsuarioNaoEntrado(){
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