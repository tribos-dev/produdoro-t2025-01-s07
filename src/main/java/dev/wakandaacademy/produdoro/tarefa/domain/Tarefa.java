package dev.wakandaacademy.produdoro.tarefa.domain;

import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaRequest;
import dev.wakandaacademy.produdoro.usuario.domain.StatusUsuario;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.http.HttpStatus;

import javax.validation.constraints.NotBlank;
import java.util.UUID;

@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Document(collection = "Tarefa")
public class Tarefa {
    @Id
    private UUID idTarefa;
    @NotBlank
    private String descricao;
    @Indexed
    private UUID idUsuario;
    @Indexed
    private UUID idArea;
    @Indexed
    private UUID idProjeto;
    private StatusTarefa status;
    private StatusAtivacaoTarefa statusAtivacao;
    private int contagemPomodoro;
    private int posicaoTarefa;

    public Tarefa(TarefaRequest tarefaRequest, int posicaoTarefa) {
        this.idTarefa = UUID.randomUUID();
        this.idUsuario = tarefaRequest.getIdUsuario();
        this.descricao = tarefaRequest.getDescricao();
        this.idArea = tarefaRequest.getIdArea();
        this.idProjeto = tarefaRequest.getIdProjeto();
        this.status = StatusTarefa.A_FAZER;
        this.statusAtivacao = StatusAtivacaoTarefa.INATIVA;
        this.contagemPomodoro = 1;
        this.posicaoTarefa = posicaoTarefa;
    }

    public void pertenceAoUsuario(Usuario usuarioPorEmail) {
        if (!this.idUsuario.equals(usuarioPorEmail.getIdUsuario())) {
            throw APIException.build(HttpStatus.UNAUTHORIZED, "Usuário não é dono da Tarefa solicitada!");
        }
    }

    public void mudaStatusParaConcluida(Usuario usuario) {
        pertenceAoUsuario(usuario);
        this.status = StatusTarefa.CONCLUIDA;
    }

    public void mudaDescricao(UUID idUsuario, String novaDescricao) {
        validaUsuario(idUsuario);
        validaSeDescricaoNaoEstaVazia(novaDescricao);
        this.descricao = novaDescricao;
    }

    private void validaUsuario(UUID idUsuario) {
        if (!this.idUsuario.equals(idUsuario)) {
            throw APIException.build(HttpStatus.UNAUTHORIZED, "credencial de autenticação não é valida");
        }
    }

   public void alteraPosicao(int novaPosicaoValue) {
        this.posicaoTarefa = novaPosicaoValue;
    }

	public void validaSeDescricaoNaoEstaVazia(String descricao){
		if (descricao == null || descricao.isEmpty()) {
			throw APIException.build(HttpStatus.BAD_REQUEST, "O campo descrição não pode estar vazio");
		}
	}

    public void incrementaPomodoro(Usuario usuario) {
		pertenceAoUsuario(usuario);
		verificaSeUsuarioEstaEmFoco(usuario);
		ativaTarefa();
		this.contagemPomodoro++;
		verificaQuantidadePomodoro(usuario);
    }

	private void verificaQuantidadePomodoro(Usuario usuario) {
		int totalPomodoro = this.contagemPomodoro;
		if (totalPomodoro %4 == 0) {
			usuario.mudaStatusParaPausaLonga(usuario.getIdUsuario());
		}else {
			usuario.mudaStatusParaPausaCurta(usuario.getIdUsuario());
		}
	}

	private void ativaTarefa() {
		this.statusAtivacao = StatusAtivacaoTarefa.ATIVA;
	}

	private void verificaSeUsuarioEstaEmFoco(Usuario usuario) {
		if (!usuario.getStatus().equals(StatusUsuario.FOCO)){
			throw APIException.build(HttpStatus.CONFLICT, "O Usuário não está em foco!");
		}
	}
}
