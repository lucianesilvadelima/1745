package br.com.ada.lms.excecao;

import jakarta.ws.rs.core.Response;

/** 400 Bad Request: os dados enviados pelo cliente estão errados. */
public class RequisicaoInvalidaException extends ApiException {

    public RequisicaoInvalidaException(String mensagem) {
        super(Response.Status.BAD_REQUEST, mensagem);
    }
}
