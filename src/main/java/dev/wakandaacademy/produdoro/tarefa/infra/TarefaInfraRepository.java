package dev.wakandaacademy.produdoro.tarefa.infra;

import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.tarefa.application.api.NovaPosicaoRequest;
import dev.wakandaacademy.produdoro.tarefa.application.repository.TarefaRepository;
import dev.wakandaacademy.produdoro.tarefa.domain.StatusTarefa;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Repository
@Log4j2
@RequiredArgsConstructor
public class TarefaInfraRepository implements TarefaRepository {

    private final TarefaSpringMongoDBRepository tarefaSpringMongoDBRepository;
    private final MongoTemplate mongoTemplate;

    private static final String POSICAO_INVALIDA_MENSAGEM =
            "Posição da tarefa deve ser menor que a quantidade total de tarefas (%d)";
    private static final String POSICAO_IGUAL_MENSAGEM =
            "A nova posição não pode ser igual à posição atual (%d)";

    @Override
    public Tarefa salva(Tarefa tarefa) {
        log.info("[inicia] TarefaInfraRepository - salva");
        try {
            tarefaSpringMongoDBRepository.save(tarefa);
        } catch (DataIntegrityViolationException e) {
            throw APIException.build(HttpStatus.BAD_REQUEST, "Tarefa já cadastrada", e);
        }
        log.info("[finaliza] TarefaInfraRepository - salva");
        return tarefa;
    }

    @Override
    public Optional<Tarefa> buscaTarefaPorId(UUID idTarefa) {
        log.info("[inicia] TarefaInfraRepository - buscaTarefaPorId");
        Optional<Tarefa> tarefaPorId = tarefaSpringMongoDBRepository.findByIdTarefa(idTarefa);
        log.info("[finaliza] TarefaInfraRepository - buscaTarefaPorId");
        return tarefaPorId;
    }

    @Override
    public List<Tarefa> buscaTarefasPorUsuario(UUID idUsuario) {
        log.info("[inicia] TarefaInfraRepository - buscaTarefasPorUsuario");
        List<Tarefa> tarefasPorUsuario = tarefaSpringMongoDBRepository.findTarefasByIdUsuario(idUsuario);
        log.info("[finaliza] TarefaInfraRepository - buscaTarefasPorUsuario");
        return tarefasPorUsuario;
    }

    @Override
    public void deletaTarefasConcluidas(List<Tarefa> tarefasConcluidas) {
        log.info("[inicia] TarefaInfraRepository - deletaTarefasConcluidas");
        try {
            tarefaSpringMongoDBRepository.deleteAll(tarefasConcluidas);
        } catch (DataIntegrityViolationException e) {
            throw APIException.build(HttpStatus.BAD_REQUEST, "Erro ao deletar tarefas concluídas", e);
        }
        log.info("[finaliza] TarefaInfraRepository - deletaTarefasConcluidas");

    }

    @Override
    public List<Tarefa> buscaTarefasConcluidas(UUID idUsuario) {
        log.info("[inicia] TarefaInfraRepository - buscaTarefasConcluidas");
        List<Tarefa> tarefasConcluidas = tarefaSpringMongoDBRepository.findAllByIdUsuarioAndStatus(idUsuario, StatusTarefa.CONCLUIDA);
        log.info("[finaliza] TarefaInfraRepository - buscaTarefasConcluidas");
        return tarefasConcluidas;
    }


    @Override
    public void deletaTodasTarefas(List<Tarefa> tarefas) {
        log.info("[inicia] TarefaInfraRepository - deletaTodasTarefas");
        tarefaSpringMongoDBRepository.deleteAll(tarefas);
        log.info("[finaliza] TarefaInfraRepository - deletaTodasTarefas");
    }

    @Override
    public int obterPosicaoParaNovaTarefa(UUID idUsuario) {
        log.info("[start] TarefaInfraRepository - obterPosicaoParaNovaTarefa");
        int totalTarefas = tarefaSpringMongoDBRepository.countByIdUsuario(idUsuario);
        log.debug("[finish] TarefaInfraRepository - obterPosicaoParaNovaTarefa");
        return totalTarefas;
    }

    @Override
    public void novaPosicaoTarefa(Tarefa tarefa, List<Tarefa> todasTarefas, NovaPosicaoRequest novaPosicao) {
        log.info("[start] TarefaInfraRepository - atualizaPosicaoTarefa");
        validaNovaPosicao(tarefa, todasTarefas, novaPosicao);

        final int posicaoAtual = tarefa.getPosicaoTarefa();
        final int novaPosicaoValue = novaPosicao.getNovaPosicao();

        if (novaPosicaoValue != posicaoAtual) {
            todasTarefas.sort(Comparator.comparingInt(Tarefa::getPosicaoTarefa));

            atualizaTarefasEntrePosicoes(todasTarefas, posicaoAtual, novaPosicaoValue);
            tarefa.alteraPosicao(novaPosicaoValue);
            tarefaSpringMongoDBRepository.save(tarefa);
        }
        log.debug("[finish] TarefaInfraRepository - atualizaPosicaoTarefa");
    }

    private void atualizaTarefasEntrePosicoes(List<Tarefa> tarefas, int posicaoOrigem, int posicaoDestino) {
        log.info("[start] TarefaInfraRepository - atualizaTarefasEntrePosicoes");
        tarefas.sort(Comparator.comparingInt(Tarefa::getPosicaoTarefa));

        final int incremento = posicaoDestino > posicaoOrigem ? -1 : 1;
        final int inicio = posicaoDestino > posicaoOrigem ? posicaoOrigem + 1 : posicaoDestino;
        final int fim = posicaoDestino > posicaoOrigem ? posicaoDestino : posicaoOrigem - 1;

        List<Tarefa> tarefasParaAtualizar = IntStream.rangeClosed(inicio, fim)
                .mapToObj(i -> {
                    Tarefa t = tarefas.get(i);
                    t.alteraPosicao(i + incremento);
                    return t;
                })
                .collect(Collectors.toList());
        tarefasParaAtualizar.forEach(t -> atualizaPosicaoTarefa(t, t.getPosicaoTarefa()));

        log.info("[finish] TarefaInfraRepository - atualizaTarefasEntrePosicoes");
    }

    private void atualizaPosicaoTarefa(Tarefa tarefa, int novaPosicao) {
        mongoTemplate.updateFirst(
                Query.query(Criteria.where("idTarefa").is(tarefa.getIdTarefa())),
                new Update().set("posicaoTarefa", novaPosicao),
                Tarefa.class
        );
    }

    private void validaNovaPosicao(Tarefa tarefa, List<Tarefa> todasTarefas, NovaPosicaoRequest novaPosicao) {
        log.info("[start] TarefaInfraRepository - validaNovaPosicao");
        int posicaoAtual = tarefa.getPosicaoTarefa();
        int novaPosicaoValue = novaPosicao.getNovaPosicao();
        int totalTarefas = todasTarefas.size();

        validarLimitesPosicao(novaPosicaoValue, totalTarefas, posicaoAtual);
        log.info("[finish] TarefaInfraRepository - validaNovaPosicao");
    }

    private void validarLimitesPosicao(int novaPosicao, int totalTarefas, int posicaoAtual) {
        log.info("[start] TarefaInfraRepository - validarLimitesPosicao");
        if (novaPosicao >= totalTarefas) {
            throw APIException.build(HttpStatus.BAD_REQUEST,
                    String.format(POSICAO_INVALIDA_MENSAGEM, totalTarefas));
        }
        if (novaPosicao == posicaoAtual) {
            throw APIException.build(HttpStatus.BAD_REQUEST,
                    String.format(POSICAO_IGUAL_MENSAGEM, posicaoAtual));
        }
        log.info("[finish] TarefaInfraRepository - validarLimitesPosicao");
    }
}
