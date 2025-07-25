package dev.wakandaacademy.produdoro.tarefa.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dev.wakandaacademy.produdoro.DataHelper;
import dev.wakandaacademy.produdoro.config.security.service.TokenService;
import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.StatusUsuario;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaIdResponse;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaRequest;
import dev.wakandaacademy.produdoro.tarefa.application.repository.TarefaRepository;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;

@ExtendWith(MockitoExtension.class)
class TarefaApplicationServiceTest {

    //	@Autowired
    @InjectMocks
    TarefaApplicationService tarefaApplicationService;

    //	@MockBean
    @Mock
    TarefaRepository tarefaRepository;

    @Mock
    UsuarioRepository usuarioRepository;

    @Mock
    TokenService tokenService;

    @Test
    void deveRetornarIdTarefaNovaCriada() {
        TarefaRequest request = getTarefaRequest();
        when(tarefaRepository.salva(any())).thenReturn(new Tarefa(request));

        TarefaIdResponse response = tarefaApplicationService.criaNovaTarefa(request);

        assertNotNull(response);
        assertEquals(TarefaIdResponse.class, response.getClass());
        assertEquals(UUID.class, response.getIdTarefa().getClass());
    }



    public TarefaRequest getTarefaRequest() {
        TarefaRequest request = new TarefaRequest("tarefa 1", UUID.randomUUID(), null, null, 0);
        return request;
    }

    @Test
    void deveIncrementarUmPomodoroATarefa(){
        //cenario
        Usuario usuario = DataHelper.createUsuarioFoco();
        Tarefa tarefa = DataHelper.createTarefa();
        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefaPorId(tarefa.getIdTarefa())).thenReturn(Optional.of(tarefa));
        when(usuarioRepository.salva(usuario)).thenReturn(usuario);
        when(tarefaRepository.salva(tarefa)).thenReturn(tarefa);

        //acao
        tarefaApplicationService.incrementaPomodoro(usuario.getEmail(), tarefa.getIdTarefa());

        //verificacao
        assertEquals(2, tarefa.getContagemPomodoro(), "Deveria incrementar 1 pomodoro");

        verify(usuarioRepository, times(2)).buscaUsuarioPorEmail(usuario.getEmail());
        verify(tarefaRepository).buscaTarefaPorId(tarefa.getIdTarefa());
        verify(usuarioRepository).salva(usuario);
        verify(tarefaRepository).salva(tarefa);
    }

    @Test
    void naoDeveIncrementarPomodoro_tarefaNaoEncontrada(){
        //cenario
        Usuario usuario = DataHelper.createUsuarioFoco();
        UUID idTarefaInvalido = UUID.randomUUID();
        when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefaPorId(idTarefaInvalido)).thenReturn(Optional.empty());

        //acao
        APIException exception = assertThrows(APIException.class,
                () -> tarefaApplicationService.incrementaPomodoro(usuario.getEmail(), idTarefaInvalido));

        //verificacao
        assertEquals("Tarefa não encontrada!", exception.getBodyException().getMessage());
        verify(usuarioRepository, times(1)).buscaUsuarioPorEmail(usuario.getEmail());
        verify(tarefaRepository, times(1)).buscaTarefaPorId(idTarefaInvalido);
    }

    @Test
    void naoDeveIncrementarPomodoro_usuarioNaoAutorizado() {
        //cenario
        Usuario usuarioNaoDonoTarefa = Usuario.builder()
                .idUsuario(UUID.randomUUID())
                .email("naoDono@email.com")
                .status(StatusUsuario.FOCO)
                .build();

        Tarefa tarefa = DataHelper.createTarefa();

        when(usuarioRepository.buscaUsuarioPorEmail(usuarioNaoDonoTarefa.getEmail())).thenReturn(usuarioNaoDonoTarefa);
        when(tarefaRepository.buscaTarefaPorId(tarefa.getIdTarefa())).thenReturn(Optional.of(tarefa));

        //acao
        APIException exception = assertThrows(APIException.class, () -> tarefaApplicationService.incrementaPomodoro(usuarioNaoDonoTarefa.getEmail(), tarefa.getIdTarefa()));

        //vericacao
        assertEquals(401, exception.getStatusException());
        assertEquals("Usuário não é o dono da Tarefa solicitada!", exception.getMessage());

        verify(usuarioRepository, times(1)).buscaUsuarioPorEmail(usuarioNaoDonoTarefa.getEmail());
        verify(tarefaRepository, times(1)).buscaTarefaPorId(tarefa.getIdTarefa());
    }
}
