package dev.wakandaacademy.produdoro.tarefa.application.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/tarefa")
public interface TarefaAPI {
    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    TarefaIdResponse postNovaTarefa(@RequestBody @Valid TarefaRequest tarefaRequest);

    @GetMapping("/{idTarefa}")
    @ResponseStatus(code = HttpStatus.OK)
    TarefaDetalhadoResponse detalhaTarefa(@RequestHeader(name = "Authorization",required = true) String token,
                      @PathVariable UUID idTarefa);

    @PatchMapping("/conclui-tarefa/{idTarefa}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    void concluiTarefa(@RequestHeader(name = "Authorization", required = true) String token,
                       @PathVariable UUID idTarefa);

    @PatchMapping("/{idTarefa}/editaTarefa")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    void editaTarefa(@RequestHeader(name = "Authorization", required = true) String token,
                     @PathVariable UUID idTarefa,
                     @RequestBody @Valid EditaTarefaRequest editaTarefaRequest);


    @DeleteMapping("/deleta-todas-tarefas/{idUsuario}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    void deletaTodasTarefas(@RequestHeader(name = "Authorization", required = true) String token,
                            @PathVariable UUID idUsuario);

    @GetMapping("/usuario/{idUsuario}")
    @ResponseStatus(code = HttpStatus.OK)
    List<TarefaListResponse> getTodasTarefasDoUsuário(@RequestHeader(name = "Authorization", required = true) String token,
                                                      @PathVariable UUID idUsuario);

    @DeleteMapping("/deleta-tarefas-concluidas/{idUsuario}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    void deletaTarefasConcluidaas(@RequestHeader(name = "Authorization" , required = true) String token,
                      @PathVariable UUID idUsuario);



    @PatchMapping("/modifica-ordem-tarefa/{idTarefa}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    void alteraPosicaoTarefa(@RequestHeader(name = "Authorization", required = true) String token,
                             @PathVariable UUID idTarefa, @Valid @RequestBody NovaPosicaoRequest novaPosicao);
    @PostMapping("/incrementa-pomodoro/{idTarefa}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    void incrementaPomodoro(@RequestHeader(name = "Authorization", required = true) String token,
                            @PathVariable UUID idTarefa);
}
