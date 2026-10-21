package br.com.ada.lms.repositorio;

import br.com.ada.lms.modelo.Docente;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DocenteRepository implements PanacheRepository<Docente> {

    public boolean emailEmUso(String email, Long idAtual) {
        if (idAtual == null) {
            return count("lower(email) = lower(?1)", email) > 0;
        }
        return count("lower(email) = lower(?1) and id <> ?2", email, idAtual) > 0;
    }
}
