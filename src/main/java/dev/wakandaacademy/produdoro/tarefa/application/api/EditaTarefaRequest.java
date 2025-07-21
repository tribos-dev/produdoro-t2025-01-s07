package dev.wakandaacademy.produdoro.tarefa.application.api;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
public class EditaTarefaRequest {
    @NotBlank
    @Size(message = "O campo descrição não pode estar vazio", max = 255, min = 3)
    private String descricao;
}
