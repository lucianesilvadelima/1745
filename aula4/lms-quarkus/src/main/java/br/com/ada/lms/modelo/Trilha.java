package br.com.ada.lms.modelo;

/** Uma trilha de aprendizagem, ex.: "Back-end Java" com 120 horas. */
public class Trilha {

    private Long id;
    private String nome;
    private String descricao;
    private Integer cargaHoraria;

    public Trilha(Long id, String nome, String descricao, Integer cargaHoraria) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.cargaHoraria = cargaHoraria;
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

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Integer getCargaHoraria() {
        return cargaHoraria;
    }

    public void setCargaHoraria(Integer cargaHoraria) {
        this.cargaHoraria = cargaHoraria;
    }
}
