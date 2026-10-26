package br.com.ada.lms.repositorio;

import br.com.ada.lms.modelo.Trilha;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TrilhaRepository implements PanacheRepository<Trilha> {

    public boolean nomeEmUso(String nome, Long idAtual) {
        if (idAtual == null) {
            return count("lower(nome) = lower(?1)", nome) > 0;
        }
        return count("lower(nome) = lower(?1) and id <> ?2", nome, idAtual) > 0;
    }

    public boolean existeComDocente(Long docenteId) {
        return count("docente.id", docenteId) > 0;
    }

    /** "join t.alunos a": percorre a tabela de junção trilha_aluno. */
    public boolean existeComAluno(Long alunoId) {
        return count("select count(t) from Trilha t join t.alunos a where a.id = ?1", alunoId) > 0;
    }
}
