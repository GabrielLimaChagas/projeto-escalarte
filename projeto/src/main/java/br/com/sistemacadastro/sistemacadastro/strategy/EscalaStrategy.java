package br.com.sistemacadastro.sistemacadastro.strategy;

import br.com.sistemacadastro.sistemacadastro.model.Colaborador;
import br.com.sistemacadastro.sistemacadastro.model.Escalas;

/**
 * Define o comportamento para decidir se o colaborador
 * deve trabalhar (escala) ou folgar em um dia.
 */
public interface EscalaStrategy {
    boolean deveTrabalhar(Colaborador colaborador, Escalas escala);
}
