package br.com.ada.lms.servico;

import br.com.ada.lms.excecao.ConflitoException;
import br.com.ada.lms.excecao.RecursoNaoEncontradoException;
import br.com.ada.lms.modelo.Docente;
import br.com.ada.lms.rest.dto.DocenteRequest;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** Mesmo padrão do AlunoService. */
@ApplicationScoped
public class DocenteService {

    private final Map<Long, Docente> docentes = new ConcurrentHashMap<>();
    private final AtomicLong sequencia = new AtomicLong();
    private final ValidadorPatch validadorPatch;

    public DocenteService(ValidadorPatch validadorPatch) {
        this.validadorPatch = validadorPatch;
        criar(new DocenteRequest("Carla Mendes", "carla@ada.tech", "Java"));
        criar(new DocenteRequest("Diego Rocha", "diego@ada.tech", "Banco de Dados"));
    }

    public List<Docente> listar() {
        return docentes.values().stream()
                .sorted(Comparator.comparing(Docente::getId))
                .toList();
    }

    public Docente buscar(Long id) {
        Docente docente = docentes.get(id);
        if (docente == null) {
            throw new RecursoNaoEncontradoException("Docente", id);
        }
        return docente;
    }

    public Docente criar(DocenteRequest dados) {
        verificarEmailDisponivel(dados.email(), null);
        Docente docente = new Docente(sequencia.incrementAndGet(), dados.nome(), dados.email(), dados.especialidade());
        docentes.put(docente.getId(), docente);
        return docente;
    }

    public Docente atualizar(Long id, DocenteRequest dados) {
        Docente docente = buscar(id);
        verificarEmailDisponivel(dados.email(), id);
        docente.setNome(dados.nome());
        docente.setEmail(dados.email());
        docente.setEspecialidade(dados.especialidade());
        return docente;
    }

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

    public void remover(Long id) {
        if (docentes.remove(id) == null) {
            throw new RecursoNaoEncontradoException("Docente", id);
        }
    }

    private void verificarEmailDisponivel(String email, Long idAtual) {
        boolean emUso = docentes.values().stream()
                .anyMatch(d -> d.getEmail().equalsIgnoreCase(email) && !d.getId().equals(idAtual));
        if (emUso) {
            throw new ConflitoException("Já existe um docente com o email " + email + ".");
        }
    }
}
