package br.com.sistemacadastro.sistemacadastro.strategy;

import br.com.sistemacadastro.sistemacadastro.model.Colaborador;
import br.com.sistemacadastro.sistemacadastro.model.Escalas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Contexto que usa o strategy apropriado para decidir
 * se o colaborador deve trabalhar ou folgar.
 */
@Component
public class EscalaStrategyContext {

    private final EscalaStrategy strategy;

    @Autowired
    public EscalaStrategyContext(CLTEscalaStrategy cltStrategy) {
        // se no futuro houver outros tipos (ex: Plantão), pode trocar a injeção
        this.strategy = cltStrategy;
    }

    public boolean deveTrabalhar(Colaborador colaborador, Escalas escala) {
        return strategy.deveTrabalhar(colaborador, escala);
    }
}
