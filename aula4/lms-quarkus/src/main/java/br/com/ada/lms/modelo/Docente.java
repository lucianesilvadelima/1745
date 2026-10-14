package br.com.ada.lms.modelo;

public class Docente {

    private Long id;
    private String nome;
    private String email;
    private String especialidade;

    public Docente(Long id, String nome, String email, String especialidade) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.especialidade = especialidade;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEspecialidade() {
        return especialidade;
    }

    public void setEspecialidade(String especialidade) {
        this.especialidade = especialidade;
    }
}
