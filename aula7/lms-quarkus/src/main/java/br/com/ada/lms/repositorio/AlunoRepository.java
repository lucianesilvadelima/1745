package br.com.ada.lms.repositorio;

import br.com.ada.lms.modelo.Aluno;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Acesso à tabela "aluno". Só de implementar PanacheRepository já ganhamos
 * listAll(), findById(), persist(), deleteById(), count()...
 */
@ApplicationScoped
public class AlunoRepository implements PanacheRepository<Aluno> {

    /** Existe OUTRO aluno (id diferente de idAtual) com este email? idAtual = null no cadastro. */
    public boolean emailEmUso(String email, Long idAtual) {
        if (idAtual == null) {
            return count("lower(email) = lower(?1)", email) > 0;
        }
        return count("lower(email) = lower(?1) and id <> ?2", email, idAtual) > 0;
    }
}
