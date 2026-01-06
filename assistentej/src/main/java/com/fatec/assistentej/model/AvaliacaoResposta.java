package com.fatec.assistentej.model;

public class AvaliacaoResposta {
    public boolean responde;
    public int nota;
    public boolean fidelidade;
    public String comentario;

    @Override
    public String toString() {
        return "AvaliacaoResposta{" +
                "responde=" + responde +
                ", nota=" + nota +
                ", fidelidade=" + fidelidade +
                ", comentario='" + comentario + '\'' +
                '}';
    }
}
