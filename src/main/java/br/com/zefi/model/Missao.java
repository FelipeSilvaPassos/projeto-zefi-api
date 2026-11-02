package br.com.zefi.model;

public class Missao {
    private int id;
    private String nome;
    private String categoria;
    private int pontosRecompensa;

    public Missao() {}

    public Missao(String nome, String categoria, int pontosRecompensa) {
        this.nome = nome;
        this.categoria = categoria;
        this.pontosRecompensa = pontosRecompensa;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public int getPontosRecompensa() { return pontosRecompensa; }
    public void setPontosRecompensa(int pontosRecompensa) { this.pontosRecompensa = pontosRecompensa; }

    @Override
    public String toString() {
        return nome + " (" + categoria + ") - " + pontosRecompensa + " pontos";
    }
}
