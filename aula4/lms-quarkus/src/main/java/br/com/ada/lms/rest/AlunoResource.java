package br.com.ada.lms.rest;

import br.com.ada.lms.modelo.Aluno;
import br.com.ada.lms.rest.dto.AlunoRequest;
import br.com.ada.lms.servico.AlunoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.List;

/**
 * CRUD de alunos.
 *
 *   GET    /alunos       -> 200 lista
 *   GET    /alunos/{id}  -> 200 aluno | 404
 *   POST   /alunos       -> 201 aluno criado + header Location | 400 | 409
 *   PUT    /alunos/{id}  -> 200 aluno atualizado (todos os campos) | 400 | 404 | 409
 *   PATCH  /alunos/{id}  -> 200 aluno atualizado (só campos enviados) | 400 | 404 | 409
 *   DELETE /alunos/{id}  -> 204 sem corpo | 404
 *
 * Os 4xx são gerados por exceções (ApiExceptionMapper) e pela validação (@Valid).
 */
@Path("/alunos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AlunoResource {

    private final AlunoService service;

    public AlunoResource(AlunoService service) {
        this.service = service;
    }

    @GET
    public List<Aluno> listar() {
        return service.listar();
    }

    @GET
    @Path("/{id}")
    public Aluno buscar(@PathParam("id") Long id) {
        return service.buscar(id);
    }

    /**
     * Aqui retornamos Response (e não Aluno) porque queremos controlar o status:
     * 201 Created + header Location apontando para o novo recurso.
     */
    @POST
    public Response criar(@NotNull @Valid AlunoRequest dados, @Context UriInfo uriInfo) {
        Aluno aluno = service.criar(dados);
        URI location = uriInfo.getAbsolutePathBuilder().path(aluno.getId().toString()).build();
        return Response.created(location).entity(aluno).build();
    }

    @PUT
    @Path("/{id}")
    public Aluno atualizar(@PathParam("id") Long id, @NotNull @Valid AlunoRequest dados) {
        return service.atualizar(id, dados);
    }

    @PATCH
    @Path("/{id}")
    public Aluno atualizarParcialmente(@PathParam("id") Long id, AlunoRequest dados) {
        return service.atualizarParcialmente(id, dados);
    }

    @DELETE
    @Path("/{id}")
    public Response remover(@PathParam("id") Long id) {
        service.remover(id);
        return Response.noContent().build();
    }
}
