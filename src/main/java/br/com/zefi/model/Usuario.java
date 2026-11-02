package br.com.zefi.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Usuario {
    private String cpf;       // identificador principal
    /* WRITE_ONLY: a senha e recebida no cadastro, mas nunca e devolvida no JSON */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String senha;     // guarda o hash (BCrypt)
    private String nome;
    private String email;
    private String cidade;
    private int pontosAcumulados;
    private String categoriaInteresse;

    public Usuario() {}

    public Usuario(String cpf, String senha, String nome, String email, String cidade, String categoriaInteresse) {
        this.cpf = cpf;
        this.senha = senha;
        this.nome = nome;
        this.email = email;
        this.cidade = cidade;
        this.categoriaInteresse = categoriaInteresse;
    }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public int getPontosAcumulados() { return pontosAcumulados; }
    public void setPontosAcumulados(int pontosAcumulados) { this.pontosAcumulados = pontosAcumulados; }

    public String getCategoriaInteresse() { return categoriaInteresse; }
    public void setCategoriaInteresse(String categoriaInteresse) { this.categoriaInteresse = categoriaInteresse; }
}
