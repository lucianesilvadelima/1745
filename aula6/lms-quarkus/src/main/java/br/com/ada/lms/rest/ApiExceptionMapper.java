package br.com.ada.lms.rest;

import br.com.ada.lms.excecao.ApiException;
import br.com.ada.lms.rest.dto.ErroDTO;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Captura qualquer ApiException lançada pelos services/resources e devolve
 * o status HTTP correspondente com um JSON de erro. Assim ninguém precisa de try/catch.
 */
@Provider
public class ApiExceptionMapper implements ExceptionMapper<ApiException> {

    @Override
    public Response toResponse(ApiException e) {
        return Response.status(e.getStatus())
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErroDTO(e.getStatus().getStatusCode(), e.getMessage()))
                .build();
    }
}
