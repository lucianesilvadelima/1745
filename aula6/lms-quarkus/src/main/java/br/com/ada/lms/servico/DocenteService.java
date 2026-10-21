package br.com.ada.lms.servico;

import br.com.ada.lms.excecao.ConflitoException;
import br.com.ada.lms.excecao.RecursoNaoEncontradoException;
import br.com.ada.lms.modelo.Docente;
import br.com.ada.lms.repositorio.DocenteRepository;
import br.com.ada.lms.rest.dto.DocenteRequest;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

/** Mesmo padrão do AlunoService. */
@ApplicationScoped
public class DocenteService {

    private final DocenteRepository repository;
    private final ValidadorPatch validadorPatch;

    public DocenteService(DocenteRepository repository, ValidadorPatch validadorPatch) {
        this.repository = repository;
        this.validadorPatch = validadorPatch;
    }

    public List<Docente> listar() {
        return repository.listAll(Sort.by("id"));
    }

    public Docente buscar(Long id) {
        return repository.findByIdOptional(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Docente", id));
    }

    @Transactional
    public Docente criar(DocenteRequest dados) {
        verificarEmailDisponivel(dados.email(), null);
        Docente docente = new Docente(dados.nome(), dados.email(), dados.especialidade());
        repository.persist(docente);
        return docente;
    }

    @Transactional
    public Docente atualizar(Long id, DocenteRequest dados) {
        Docente docente = buscar(id);
        verificarEmailDisponivel(dados.email(), id);
        docente.setNome(dados.nome());
        docente.setEmail(dados.email());
        docente.setEspecialidade(dados.especialidade());
        return docente;
    }

    @Transactional
    public Docente atualizarParcialmente(Long id, DocenteRequest dados) {
        Docente docente = buscar(id);
        validadorPatch.validarCamposInformados(dados);
        if (dados.nome() != null) {
            docente.setNome(dados.nome());
        }
        if (dados.email() != null) {
            verificarEmailDisponivel(dados.email(), id);
            docente.setEmail(dados.email());
        }
        if (dados.especialidade() != null) {
            docente.setEspecialidade(dados.especialidade());
        }
        return docente;
    }

    @Transactional
    public void remover(Long id) {
        if (!repository.deleteById(id)) {
            throw new RecursoNaoEncontradoException("Docente", id);
        }
    }

    private void verificarEmailDisponivel(String email, Long idAtual) {
        if (repository.emailEmUso(email, idAtual)) {
            throw new ConflitoException("Já existe um docente com o email " + email + ".");
        }
    }
}
