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
}
