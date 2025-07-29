package dev.wakandaacademy.produdoro.tarefa.application.api;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EditaTarefaRequest {
    private String descricao = "";

    public EditaTarefaRequest(String descricao) {
        this.descricao = descricao;
    }
}

