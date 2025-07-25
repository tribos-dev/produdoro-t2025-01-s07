package dev.wakandaacademy.produdoro.tarefa.application.service;

import dev.wakandaacademy.produdoro.tarefa.application.api.*;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

public interface TarefaService {
    TarefaIdResponse criaNovaTarefa(TarefaRequest tarefaRequest);

    Tarefa detalhaTarefa(String usuario, UUID idTarefa);

    void concluiTarefa(String emailUsuario, UUID idTarefa);

    List<TarefaListResponse> getTodasTarefasDoUsuario(String usuario, UUID idUsuario);

    void editaTarefa(String emailUsuario, UUID idTarefa, @Valid EditaTarefaRequest editaTarefaRequest);

    void atualizaPosicaoTarefa(String emailUsuario, UUID idTarefa, TarefaNovaPosicaoRequest novaPosicao);
}
