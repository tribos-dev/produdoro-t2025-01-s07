package dev.wakandaacademy.produdoro.tarefa.application.service;

import dev.wakandaacademy.produdoro.tarefa.application.api.EditaTarefaRequest;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaIdResponse;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaListResponse;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaRequest;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;

import java.util.List;
import javax.validation.Valid;
import java.util.UUID;
public interface TarefaService {
    TarefaIdResponse criaNovaTarefa(TarefaRequest tarefaRequest);
    Tarefa detalhaTarefa(String usuario, UUID idTarefa);
    void concluiTarefa(String emailUsuario, UUID idTarefa);
    List<TarefaListResponse> getTodasTarefasDoUsuario(String usuario, UUID idUsuario);

    void editaTarefa(String emailUsuario, UUID idTarefa, @Valid EditaTarefaRequest editaTarefaRequest);
}
