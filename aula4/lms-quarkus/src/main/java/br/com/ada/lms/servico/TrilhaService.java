package br.com.ada.lms.servico;

import br.com.ada.lms.excecao.ConflitoException;
import br.com.ada.lms.excecao.RecursoNaoEncontradoException;
import br.com.ada.lms.modelo.Trilha;
import br.com.ada.lms.rest.dto.TrilhaRequest;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** Mesmo padrão do AlunoService; aqui a regra de unicidade é pelo nome da trilha. */
@ApplicationScoped
public class TrilhaService {

    private final Map<Long, Trilha> trilhas = new ConcurrentHashMap<>();
    private final AtomicLong sequencia = new AtomicLong();
    private final ValidadorPatch validadorPatch;

    public TrilhaService(ValidadorPatch validadorPatch) {
        this.validadorPatch = validadorPatch;
        criar(new TrilhaRequest("Back-end Java", "Java, Quarkus e APIs REST", 120));
        criar(new TrilhaRequest("Front-end", "HTML, CSS e JavaScript", 80));
    }

    public List<Trilha> listar() {
        return trilhas.values().stream()
                .sorted(Comparator.comparing(Trilha::getId))
                .toList();
    }

    public Trilha buscar(Long id) {
        Trilha trilha = trilhas.get(id);
        if (trilha == null) {
            throw new RecursoNaoEncontradoException("Trilha", id);
        }
        return trilha;
    }

    public Trilha criar(TrilhaRequest dados) {
        verificarNomeDisponivel(dados.nome(), null);
        Trilha trilha = new Trilha(sequencia.incrementAndGet(), dados.nome(), dados.descricao(), dados.cargaHoraria());
        trilhas.put(trilha.getId(), trilha);
        return trilha;
    }

    public Trilha atualizar(Long id, TrilhaRequest dados) {
        Trilha trilha = buscar(id);
        verificarNomeDisponivel(dados.nome(), id);
        trilha.setNome(dados.nome());
        trilha.setDescricao(dados.descricao());
        trilha.setCargaHoraria(dados.cargaHoraria());
        return trilha;
    }

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

    public void remover(Long id) {
        if (trilhas.remove(id) == null) {
            throw new RecursoNaoEncontradoException("Trilha", id);
        }
    }

    private void verificarNomeDisponivel(String nome, Long idAtual) {
        boolean emUso = trilhas.values().stream()
                .anyMatch(t -> t.getNome().equalsIgnoreCase(nome) && !t.getId().equals(idAtual));
        if (emUso) {
            throw new ConflitoException("Já existe uma trilha com o nome " + nome + ".");
        }
    }
}
