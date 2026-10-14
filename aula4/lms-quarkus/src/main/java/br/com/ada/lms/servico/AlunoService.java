package br.com.ada.lms.servico;

import br.com.ada.lms.excecao.ConflitoException;
import br.com.ada.lms.excecao.RecursoNaoEncontradoException;
import br.com.ada.lms.modelo.Aluno;
import br.com.ada.lms.rest.dto.AlunoRequest;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Regras de negócio + "banco de dados" em memória (um Map).
 * Os dados somem quando a aplicação reinicia; persistência real fica para as próximas aulas.
 *
 * @ApplicationScoped: o Quarkus cria UMA instância e a injeta em quem pedir.
 * ConcurrentHashMap/AtomicLong porque várias requisições podem chegar ao mesmo tempo.
 */
@ApplicationScoped
public class AlunoService {

    private final Map<Long, Aluno> alunos = new ConcurrentHashMap<>();
    private final AtomicLong sequencia = new AtomicLong();
    private final ValidadorPatch validadorPatch;

    public AlunoService(ValidadorPatch validadorPatch) {
        this.validadorPatch = validadorPatch;
        // alguns dados iniciais para testar o GET logo de cara
        criar(new AlunoRequest("Ana Souza", "ana@email.com"));
        criar(new AlunoRequest("Bruno Lima", "bruno@email.com"));
    }

    public List<Aluno> listar() {
        return alunos.values().stream()
                .sorted(Comparator.comparing(Aluno::getId))
                .toList();
    }

    public Aluno buscar(Long id) {
        Aluno aluno = alunos.get(id);
        if (aluno == null) {
            throw new RecursoNaoEncontradoException("Aluno", id);
        }
        return aluno;
    }

    public Aluno criar(AlunoRequest dados) {
        verificarEmailDisponivel(dados.email(), null);
        Aluno aluno = new Aluno(sequencia.incrementAndGet(), dados.nome(), dados.email());
        alunos.put(aluno.getId(), aluno);
        return aluno;
    }

    /** PUT: substitui TODOS os campos do recurso. */
    public Aluno atualizar(Long id, AlunoRequest dados) {
        Aluno aluno = buscar(id);
        verificarEmailDisponivel(dados.email(), id);
        aluno.setNome(dados.nome());
        aluno.setEmail(dados.email());
        return aluno;
    }

    /** PATCH: altera somente os campos enviados (os que vierem null são mantidos). */
    public Aluno atualizarParcialmente(Long id, AlunoRequest dados) {
        Aluno aluno = buscar(id);
        validadorPatch.validarCamposInformados(dados);
        if (dados.nome() != null) {
            aluno.setNome(dados.nome());
        }
        if (dados.email() != null) {
            verificarEmailDisponivel(dados.email(), id);
            aluno.setEmail(dados.email());
        }
        return aluno;
    }

    public void remover(Long id) {
        if (alunos.remove(id) == null) {
            throw new RecursoNaoEncontradoException("Aluno", id);
        }
    }

    private void verificarEmailDisponivel(String email, Long idAtual) {
        boolean emUso = alunos.values().stream()
                .anyMatch(a -> a.getEmail().equalsIgnoreCase(email) && !a.getId().equals(idAtual));
        if (emUso) {
            throw new ConflitoException("Já existe um aluno com o email " + email + ".");
        }
    }
}
