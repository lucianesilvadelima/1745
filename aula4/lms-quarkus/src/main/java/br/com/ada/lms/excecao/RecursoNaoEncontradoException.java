package br.com.ada.lms.excecao;

import jakarta.ws.rs.core.Response;

/** 404 Not Found: o id informado não existe. */
public class RecursoNaoEncontradoException extends ApiException {

    public RecursoNaoEncontradoException(String recurso, Long id) {
        super(Response.Status.NOT_FOUND, recurso + " com id " + id + " não encontrado(a).");
    }
}
