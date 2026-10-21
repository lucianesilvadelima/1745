package br.com.ada.lms.servico;

import br.com.ada.lms.excecao.ConflitoException;
import br.com.ada.lms.excecao.RecursoNaoEncontradoException;
import br.com.ada.lms.modelo.Aluno;
import br.com.ada.lms.repositorio.AlunoRepository;
import br.com.ada.lms.rest.dto.AlunoRequest;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

/**
 * Regras de negócio. Os dados agora ficam no PostgreSQL: quem conversa com o banco é o AlunoRepository.
 *
 * @Transactional: tudo que o método faz no banco é confirmado (commit) junto no final,
 * ou desfeito (rollback) se uma exceção for lançada. Obrigatório para gravar/alterar/remover.
 *
 * Repare que no PUT/PATCH não chamamos nenhum "update": a entidade buscada está "gerenciada"
 * pelo Hibernate, que detecta os setters e gera o UPDATE sozinho no commit (dirty checking).
 */
@ApplicationScoped
public class AlunoService {

    private final AlunoRepository repository;
    private final ValidadorPatch validadorPatch;

    public AlunoService(AlunoRepository repository, ValidadorPatch validadorPatch) {
        this.repository = repository;
        this.validadorPatch = validadorPatch;
    }

    public List<Aluno> listar() {
        return repository.listAll(Sort.by("id"));
    }

    public Aluno buscar(Long id) {
        return repository.findByIdOptional(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aluno", id));
    }

    @Transactional
    public Aluno criar(AlunoRequest dados) {
        verificarEmailDisponivel(dados.email(), null);
        Aluno aluno = new Aluno(dados.nome(), dados.email());
        repository.persist(aluno); // INSERT; o id é preenchido pelo banco
        return aluno;
    }

    /** PUT: substitui TODOS os campos do recurso. */
    @Transactional
    public Aluno atualizar(Long id, AlunoRequest dados) {
        Aluno aluno = buscar(id);
        verificarEmailDisponivel(dados.email(), id);
        aluno.setNome(dados.nome());
        aluno.setEmail(dados.email());
        return aluno;
    }

    /** PATCH: altera somente os campos enviados (os que vierem null são mantidos). */
    @Transactional
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

    @Transactional
    public void remover(Long id) {
        if (!repository.deleteById(id)) {
            throw new RecursoNaoEncontradoException("Aluno", id);
        }
    }

    private void verificarEmailDisponivel(String email, Long idAtual) {
        if (repository.emailEmUso(email, idAtual)) {
            throw new ConflitoException("Já existe um aluno com o email " + email + ".");
        }
    }
}
