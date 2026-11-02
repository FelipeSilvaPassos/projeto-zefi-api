package br.com.zefi.model;

import java.sql.Timestamp;

/** Acao boa relatada pelo usuario no chat e avaliada pelo Zefi. */
public class AcaoSustentavel {
    private int id;
    private String cpfUsuario;
    private String descricao;
    private String categoriaDetectada;
    private int pontosAtribuidos;
    private Timestamp dataRegistro;

    public AcaoSustentavel() {}

    public AcaoSustentavel(String cpfUsuario, String descricao, String categoriaDetectada, int pontosAtribuidos) {
        this.cpfUsuario = cpfUsuario;
        this.descricao = descricao;
        this.categoriaDetectada = categoriaDetectada;
        this.pontosAtribuidos = pontosAtribuidos;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCpfUsuario() { return cpfUsuario; }
    public void setCpfUsuario(String cpfUsuario) { this.cpfUsuario = cpfUsuario; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getCategoriaDetectada() { return categoriaDetectada; }
    public void setCategoriaDetectada(String categoriaDetectada) { this.categoriaDetectada = categoriaDetectada; }

    public int getPontosAtribuidos() { return pontosAtribuidos; }
    public void setPontosAtribuidos(int pontosAtribuidos) { this.pontosAtribuidos = pontosAtribuidos; }

    public Timestamp getDataRegistro() { return dataRegistro; }
    public void setDataRegistro(Timestamp dataRegistro) { this.dataRegistro = dataRegistro; }
}
