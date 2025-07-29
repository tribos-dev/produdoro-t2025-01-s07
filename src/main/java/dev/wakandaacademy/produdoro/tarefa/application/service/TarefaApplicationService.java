package dev.wakandaacademy.produdoro.tarefa.application.service;

import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.tarefa.application.api.*;
import dev.wakandaacademy.produdoro.tarefa.application.repository.TarefaRepository;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Log4j2
@RequiredArgsConstructor
public class TarefaApplicationService implements TarefaService {
    private final TarefaRepository tarefaRepository;
    private final UsuarioRepository usuarioRepository;


    @Override
    public TarefaIdResponse criaNovaTarefa(TarefaRequest tarefaRequest) {
        log.info("[inicia] TarefaApplicationService - criaNovaTarefa");
        int posicaoTarefa = obterPosicaoParaNovaTarefa(tarefaRequest.getIdUsuario());
        Tarefa tarefaCriada = tarefaRepository.salva(new Tarefa(tarefaRequest, posicaoTarefa));
        log.info("[finaliza] TarefaApplicationService - criaNovaTarefa");
        return TarefaIdResponse.builder().idTarefa(tarefaCriada.getIdTarefa()).build();
    }

    private int obterPosicaoParaNovaTarefa(UUID idUsuario) {
        log.info("[start] TarefaApplicationService - obterPosicaoParaNovaTarefa");
        int posicaoTarefa = tarefaRepository.obterPosicaoParaNovaTarefa(idUsuario);
        log.debug("[finish] TarefaApplicationService - obterPosicaoParaNovaTarefa");
        return posicaoTarefa;
    }

    @Override
    public Tarefa detalhaTarefa(String usuario, UUID idTarefa) {
        log.info("[inicia] TarefaApplicationService - detalhaTarefa");
        Usuario usuarioPorEmail = usuarioRepository.buscaUsuarioPorEmail(usuario);
        log.info("[usuarioPorEmail] {}", usuarioPorEmail);
        Tarefa tarefa =
                tarefaRepository.buscaTarefaPorId(idTarefa).orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Tarefa não encontrada!"));
        tarefa.pertenceAoUsuario(usuarioPorEmail);
        log.info("[finaliza] TarefaApplicationService - detalhaTarefa");
        return tarefa;
    }

    @Override
    public void concluiTarefa(String emailUsuario, UUID idTarefa) {
        log.info("[inicia] TarefaApplicationService - concluiTarefa");
        Usuario usuario = usuarioRepository.buscaUsuarioPorEmail(emailUsuario);
        Tarefa tarefa = detalhaTarefa(emailUsuario, idTarefa);
        tarefa.mudaStatusParaConcluida(usuario);
        tarefaRepository.salva(tarefa);
        log.info("[finaliza] TarefaApplicationService - concluiTarefa");
    }

    @Override
    public void deletaTodasTarefas(String emailUsuario, UUID idUsuario) {
        log.info("[inicia] TarefaApplicationService - deletaTodasTarefas");
        Usuario usuarioPorEmail = usuarioRepository.buscaUsuarioPorEmail(emailUsuario);
        log.info("[usuarioPorEmail] {}", usuarioPorEmail);
        usuarioRepository.buscaUsuarioPorId(idUsuario);
        usuarioPorEmail.validaUsuario(idUsuario);
        List<Tarefa> tarefas = tarefaRepository.buscaTarefasPorUsuario(idUsuario);
        verificaListaEstaVazia(tarefas);
        verificaQuantidadeTarefas(tarefas);
        tarefaRepository.deletaTodasTarefas(tarefas);
        log.info("[finaliza] TarefaApplicationService - deletaTodasTarefas");

    }

    private void verificaQuantidadeTarefas(List<Tarefa> tarefas) {
        if (tarefas.size() < 2) {
            throw APIException.build(HttpStatus.CONFLICT,
                    "Usuário não possui quantidade minima de tarefa(as) cadastrada(as)");
        }
    }

    private void verificaListaEstaVazia(List<Tarefa> tarefas) {
        if (tarefas.isEmpty()) {
            throw APIException.build(HttpStatus.CONFLICT,
                    "Usuário não possui tarefa(as) cadastrada(as)");
        }
    }

    @Override
    public List<TarefaListResponse> getTodasTarefasDoUsuario(String usuario, UUID idUsuario) {
        log.info("[inicia] TarefaApplicationService - getTodasTarefasDoUsuario");
        Usuario usuarioPorEmail = usuarioRepository.buscaUsuarioPorEmail(usuario);
        usuarioRepository.buscaUsuarioPorId(idUsuario);
        usuarioPorEmail.validaUsuario(idUsuario);
        List<Tarefa> tarefas = tarefaRepository.buscaTarefasPorUsuario(idUsuario);
        log.info("[finaliza] TarefaApplicationService - getTodasTarefasDoUsuario");
        return TarefaListResponse.converte(tarefas);
    }

    @Override
    public void deletaTarefasConcluidas(String usuario, UUID idUsuario) {
        log.info("[inicia] TarefaApplicationService - deletaTarefasConcluidas");
        validaUsuario(usuario, idUsuario);
        List<Tarefa> tarefasConcluidas = tarefaRepository.buscaTarefasConcluidas(idUsuario);
        if (tarefasConcluidas.isEmpty()) {
            throw APIException.build(HttpStatus.NOT_FOUND, "Usuário não possui nenhuma tarefa concluída");
        }
        tarefaRepository.deletaTarefasConcluidas(tarefasConcluidas);
        log.info("[finaliza] TarefaApplicationService - deletaTarefasConcluidas");
    }

    private void validaUsuario(String usuario, UUID idUsuario) {
        Usuario usuarioPorEmail = usuarioRepository.buscaUsuarioPorEmail(usuario);
        usuarioRepository.buscaUsuarioPorId(idUsuario);
        usuarioPorEmail.validaUsuario(idUsuario);
    }

    @Override
    public void editaTarefa(String emailUsuario, UUID idTarefa, EditaTarefaRequest editaTarefaRequest) {
        log.info("[inicia] TarefaApplicationService - editaTarefa");
        Usuario usuario = usuarioRepository.buscaUsuarioPorEmail(emailUsuario);
        Tarefa tarefa = detalhaTarefa(emailUsuario, idTarefa);
        tarefa.mudaDescricao(usuario.getIdUsuario(), editaTarefaRequest.getDescricao());
        tarefaRepository.salva(tarefa);
        log.info("[finaliza] TarefaApplicationService - editaTarefa");
    }

    @Override
    public void alteraPosicaoTarefa(String emailUsuario, UUID idTarefa, NovaPosicaoRequest novaPosicao) {
        log.info("[start] TarefaApplicationService - alteraPosicaoTarefa");
        Tarefa tarefa = detalhaTarefa(emailUsuario, idTarefa);
        List<Tarefa> todasTarefas = tarefaRepository.buscaTarefasPorUsuario(tarefa.getIdUsuario());
        tarefaRepository.novaPosicaoTarefa(tarefa, todasTarefas, novaPosicao);
        log.info("[finish] TarefaApplicationService - alteraPosicaoTarefa");
    }
}
