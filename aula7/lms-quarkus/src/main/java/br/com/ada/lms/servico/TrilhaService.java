package br.com.ada.lms.servico;

import br.com.ada.lms.excecao.ConflitoException;
import br.com.ada.lms.excecao.RecursoNaoEncontradoException;
import br.com.ada.lms.modelo.Aluno;
import br.com.ada.lms.modelo.Trilha;
import br.com.ada.lms.repositorio.TrilhaRepository;
import br.com.ada.lms.rest.dto.TrilhaRequest;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

/** Mesmo padrão do AlunoService; aqui a regra de unicidade é pelo nome da trilha. */
@ApplicationScoped
public class TrilhaService {

    private final TrilhaRepository repository;
    private final ValidadorPatch validadorPatch;
    private final DocenteService docenteService;
    private final AlunoService alunoService;

    public TrilhaService(TrilhaRepository repository, ValidadorPatch validadorPatch,
                         DocenteService docenteService, AlunoService alunoService) {
        this.repository = repository;
        this.validadorPatch = validadorPatch;
        this.docenteService = docenteService;
        this.alunoService = alunoService;
    }

    public List<Trilha> listar() {
        return repository.listAll(Sort.by("id"));
    }

    public Trilha buscar(Long id) {
        return repository.findByIdOptional(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Trilha", id));
    }

    @Transactional
    public Trilha criar(TrilhaRequest dados) {
        verificarNomeDisponivel(dados.nome(), null);
        Trilha trilha = new Trilha(dados.nome(), dados.descricao(), dados.cargaHoraria());
        repository.persist(trilha);
        return trilha;
    }

    @Transactional
    public Trilha atualizar(Long id, TrilhaRequest dados) {
        Trilha trilha = buscar(id);
        verificarNomeDisponivel(dados.nome(), id);
        trilha.setNome(dados.nome());
        trilha.setDescricao(dados.descricao());
        trilha.setCargaHoraria(dados.cargaHoraria());
        return trilha;
    }

    @Transactional
    public Trilha atualizarParcialmente(Long id, TrilhaRequest dados) {
        Trilha trilha = buscar(id);
        validadorPatch.validarCamposInformados(dados);
        if (dados.nome() != null) {
            verificarNomeDisponivel(dados.nome(), id);
            trilha.setNome(dados.nome());
        }
        if (dados.descricao() != null) {
            trilha.setDescricao(dados.descricao());
        }
        if (dados.cargaHoraria() != null) {
            trilha.setCargaHoraria(dados.cargaHoraria());
        }
        return trilha;
    }

    @Transactional
    public void remover(Long id) {
        if (!repository.deleteById(id)) {
            throw new RecursoNaoEncontradoException("Trilha", id);
        }
    }

    /** Define (ou troca) o docente responsável. 404 se a trilha ou o docente não existirem. */
    @Transactional
    public Trilha definirDocente(Long id, Long docenteId) {
        Trilha trilha = buscar(id);
        trilha.setDocente(docenteService.buscar(docenteId));
        return trilha;
    }

    @Transactional
    public Trilha removerDocente(Long id) {
        Trilha trilha = buscar(id);
        trilha.setDocente(null);
        return trilha;
    }

    /** Matricula o aluno na trilha: o Hibernate grava a linha em trilha_aluno no commit. */
    @Transactional
    public Trilha matricularAluno(Long id, Long alunoId) {
        Trilha trilha = buscar(id);
        Aluno aluno = alunoService.buscar(alunoId);
        if (trilha.getAlunos().contains(aluno)) {
            throw new ConflitoException("Aluno com id " + alunoId + " já está matriculado na trilha " + trilha.getNome() + ".");
        }
        trilha.adicionarAluno(aluno);
        return trilha;
    }

    /** Desmatricula o aluno; se ele não estava matriculado, não faz nada (idempotente, como um DELETE). */
    @Transactional
    public Trilha desmatricularAluno(Long id, Long alunoId) {
        Trilha trilha = buscar(id);
        trilha.removerAluno(alunoService.buscar(alunoId));
        return trilha;
    }

    private void verificarNomeDisponivel(String nome, Long idAtual) {
        if (repository.nomeEmUso(nome, idAtual)) {
            throw new ConflitoException("Já existe uma trilha com o nome " + nome + ".");
        }
    }
}
