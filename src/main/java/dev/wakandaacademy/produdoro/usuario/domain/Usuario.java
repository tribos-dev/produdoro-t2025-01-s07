package dev.wakandaacademy.produdoro.usuario.domain;

import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.pomodoro.domain.ConfiguracaoPadrao;
import dev.wakandaacademy.produdoro.usuario.application.api.UsuarioNovoRequest;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.http.HttpStatus;

import javax.validation.constraints.Email;
import java.util.UUID;

@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString
@Document(collection = "Usuario")
public class Usuario {
    @Id
    private UUID idUsuario;
    @Email
    @Indexed(unique = true)
    private String email;
    private ConfiguracaoUsuario configuracao;
    @Builder.Default
    private StatusUsuario status = StatusUsuario.FOCO;
    @Builder.Default
    private Integer quantidadePomodorosPausaCurta = 0;

    public Usuario(UsuarioNovoRequest usuarioNovo, ConfiguracaoPadrao configuracaoPadrao) {
        this.idUsuario = UUID.randomUUID();
        this.email = usuarioNovo.getEmail();
        this.status = StatusUsuario.FOCO;
        this.configuracao = new ConfiguracaoUsuario(configuracaoPadrao);
    }

    public void mudaStatusParaFoco(UUID idUsuario) {
        validaUsuario(idUsuario);
        verificaStatus(StatusUsuario.FOCO);
        this.status = StatusUsuario.FOCO;
    }

    private void verificaStatus(StatusUsuario statusUsuario) {
        if (this.status.equals(statusUsuario)) {
            throw APIException.build(HttpStatus.CONFLICT, "Usuário já está em FOCO");
        }
    }

    public void validaUsuario(UUID idUsuario) {
        if (!this.idUsuario.equals(idUsuario)) {
            throw APIException.build(HttpStatus.UNAUTHORIZED, "credencial de autenticação não é valida");
        }
    }

    public void mudaStatusParaPausaCurta(UUID idUsuario) {
        validaUsuario(idUsuario);
        verificaStatusPausaCurta(StatusUsuario.PAUSA_CURTA);
        this.status = StatusUsuario.PAUSA_CURTA;
    }

    private void verificaStatusPausaCurta(StatusUsuario pausaCurta) {
        if (this.status.equals(StatusUsuario.PAUSA_CURTA)) {
            throw APIException.build(HttpStatus.CONFLICT, "Usuário já está em PAUSA CURTA");
        }
    }
}
