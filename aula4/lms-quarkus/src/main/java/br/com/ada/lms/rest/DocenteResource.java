package br.com.ada.lms.rest;

import br.com.ada.lms.modelo.Docente;
import br.com.ada.lms.rest.dto.DocenteRequest;
import br.com.ada.lms.servico.DocenteService;
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

/** CRUD de docentes: mesmo contrato do AlunoResource. */
@Path("/docentes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DocenteResource {

    private final DocenteService service;

    public DocenteResource(DocenteService service) {
        this.service = service;
    }

    @GET
    public List<Docente> listar() {
        return service.listar();
    }

    @GET
    @Path("/{id}")
    public Docente buscar(@PathParam("id") Long id) {
        return service.buscar(id);
    }

    @POST
    public Response criar(@NotNull @Valid DocenteRequest dados, @Context UriInfo uriInfo) {
        Docente docente = service.criar(dados);
        URI location = uriInfo.getAbsolutePathBuilder().path(docente.getId().toString()).build();
        return Response.created(location).entity(docente).build();
    }

    @PUT
    @Path("/{id}")
    public Docente atualizar(@PathParam("id") Long id, @NotNull @Valid DocenteRequest dados) {
        return service.atualizar(id, dados);
    }

    @PATCH
    @Path("/{id}")
    public Docente atualizarParcialmente(@PathParam("id") Long id, DocenteRequest dados) {
        return service.atualizarParcialmente(id, dados);
    }

    @DELETE
    @Path("/{id}")
    public Response remover(@PathParam("id") Long id) {
        service.remover(id);
        return Response.noContent().build();
    }
}
