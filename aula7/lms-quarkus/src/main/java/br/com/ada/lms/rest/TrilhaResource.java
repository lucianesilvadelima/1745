package br.com.ada.lms.rest;

import br.com.ada.lms.modelo.Trilha;
import br.com.ada.lms.rest.dto.TrilhaRequest;
import br.com.ada.lms.servico.TrilhaService;
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

/** CRUD de trilhas: mesmo contrato do AlunoResource. */
@Path("/trilhas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TrilhaResource {

    private final TrilhaService service;

    public TrilhaResource(TrilhaService service) {
        this.service = service;
    }

    @GET
    public List<Trilha> listar() {
        return service.listar();
    }

    @GET
    @Path("/{id}")
    public Trilha buscar(@PathParam("id") Long id) {
        return service.buscar(id);
    }

    @POST
    public Response criar(@NotNull @Valid TrilhaRequest dados, @Context UriInfo uriInfo) {
        Trilha trilha = service.criar(dados);
        URI location = uriInfo.getAbsolutePathBuilder().path(trilha.getId().toString()).build();
        return Response.created(location).entity(trilha).build();
    }

    @PUT
    @Path("/{id}")
    public Trilha atualizar(@PathParam("id") Long id, @NotNull @Valid TrilhaRequest dados) {
        return service.atualizar(id, dados);
    }

    @PATCH
    @Path("/{id}")
    public Trilha atualizarParcialmente(@PathParam("id") Long id, TrilhaRequest dados) {
        return service.atualizarParcialmente(id, dados);
    }

    @DELETE
    @Path("/{id}")
    public Response remover(@PathParam("id") Long id) {
        service.remover(id);
        return Response.noContent().build();
    }

    // ---------- Docente responsável ----------

    /** PUT: define (ou troca) o docente responsável. Repetir a chamada dá o mesmo resultado (idempotente). */
    @PUT
    @Path("/{id}/docente/{docenteId}")
    @Consumes(MediaType.WILDCARD) // sem corpo: aceita requisição sem Content-Type (senão 415)
    public Trilha definirDocente(@PathParam("id") Long id, @PathParam("docenteId") Long docenteId) {
        return service.definirDocente(id, docenteId);
    }

    @DELETE
    @Path("/{id}/docente")
    public Response removerDocente(@PathParam("id") Long id) {
        service.removerDocente(id);
        return Response.noContent().build();
    }

    // ---------- Alunos matriculados ----------

    /** POST: matricula o aluno (409 se ele já estiver matriculado). */
    @POST
    @Path("/{id}/alunos/{alunoId}")
    @Consumes(MediaType.WILDCARD) // sem corpo: aceita requisição sem Content-Type (senão 415)
    public Trilha matricularAluno(@PathParam("id") Long id, @PathParam("alunoId") Long alunoId) {
        return service.matricularAluno(id, alunoId);
    }

    @DELETE
    @Path("/{id}/alunos/{alunoId}")
    public Response desmatricularAluno(@PathParam("id") Long id, @PathParam("alunoId") Long alunoId) {
        service.desmatricularAluno(id, alunoId);
        return Response.noContent().build();
    }
}
