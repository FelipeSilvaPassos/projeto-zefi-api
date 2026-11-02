package br.com.zefi.model;

import java.sql.Timestamp;

/** Registro de cada missao que o Zefi sugeriu a um usuario. */
public class RecomendacaoAvatar {
    private int id;
    private String cpfUsuario;
    private int missaoSugeridaId;
    private Timestamp dataSugestao;
    private boolean aceita;

    public RecomendacaoAvatar() {}

    public RecomendacaoAvatar(String cpfUsuario, int missaoSugeridaId) {
        this.cpfUsuario = cpfUsuario;
        this.missaoSugeridaId = missaoSugeridaId;
        this.aceita = false;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCpfUsuario() { return cpfUsuario; }
    public void setCpfUsuario(String cpfUsuario) { this.cpfUsuario = cpfUsuario; }

    public int getMissaoSugeridaId() { return missaoSugeridaId; }
    public void setMissaoSugeridaId(int missaoSugeridaId) { this.missaoSugeridaId = missaoSugeridaId; }

    public Timestamp getDataSugestao() { return dataSugestao; }
    public void setDataSugestao(Timestamp dataSugestao) { this.dataSugestao = dataSugestao; }

    public boolean isAceita() { return aceita; }
    public void setAceita(boolean aceita) { this.aceita = aceita; }
}
