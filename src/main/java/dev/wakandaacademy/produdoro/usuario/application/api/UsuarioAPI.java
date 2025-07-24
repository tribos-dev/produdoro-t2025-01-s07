package dev.wakandaacademy.produdoro.usuario.application.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping(value = "/public/v1/usuario")
public interface UsuarioAPI {
    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    UsuarioCriadoResponse postNovoUsuario(@RequestBody @Valid UsuarioNovoRequest usuarioNovo);

    @GetMapping(value = "/{idUsuario}")
    @ResponseStatus(code = HttpStatus.OK)
    UsuarioCriadoResponse buscaUsuarioPorId(@PathVariable UUID idUsuario);

    @PatchMapping("/{idUsuario}/foco")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    void mudaStatusParaFoco(@RequestHeader(name = "Authorization", required = true) String token,
                            @PathVariable UUID idUsuario);

    @PatchMapping("/{idUsuario}/pausa-curta")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    void mudaStatusParaPausaCurta(@RequestHeader(name = "Authorization", required = true) String token,
                                  @PathVariable UUID idUsuario);
}
