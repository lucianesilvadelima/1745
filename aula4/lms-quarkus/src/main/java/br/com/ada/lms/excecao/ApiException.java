package br.com.ada.lms.excecao;

import jakarta.ws.rs.core.Response;

/**
 * Exceção base da API: cada subclasse já sabe qual status HTTP (4xx) representa.
 * Quem traduz a exceção em resposta HTTP é o {@link br.com.ada.lms.rest.ApiExceptionMapper}.
 */
public abstract class ApiException extends RuntimeException {

    private final Response.Status status;

    protected ApiException(Response.Status status, String mensagem) {
        super(mensagem);
        this.status = status;
    }

    public Response.Status getStatus() {
        return status;
    }
}
