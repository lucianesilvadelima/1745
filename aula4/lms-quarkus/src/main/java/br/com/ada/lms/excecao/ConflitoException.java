package br.com.ada.lms.excecao;

import jakarta.ws.rs.core.Response;

/** 409 Conflict: a requisição é válida, mas conflita com o estado atual (ex.: e-mail já cadastrado). */
public class ConflitoException extends ApiException {

    public ConflitoException(String mensagem) {
        super(Response.Status.CONFLICT, mensagem);
    }
}
