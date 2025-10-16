package br.com.sistemacadastro.sistemacadastro.strategy;

import br.com.sistemacadastro.sistemacadastro.model.Colaborador;
import br.com.sistemacadastro.sistemacadastro.model.Contrato;
import br.com.sistemacadastro.sistemacadastro.model.Escalas;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Strategy para colaboradores CLT.
 * Mantém a lógica atual de folga por dia da semana.
 */
@Component
public class CLTEscalaStrategy implements EscalaStrategy {

    @Override
    public boolean deveTrabalhar(Colaborador colaborador, Escalas escala) {
        Contrato contrato = colaborador.getContrato();

        if (contrato == null || contrato.getDiasFolga() == null)
            return true; // se não há informações de folga, assume que trabalha

        LocalDate data = escala.getDataEscala().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        DayOfWeek diaSemana = data.getDayOfWeek();

        boolean ehFolga = contrato.getDiasFolga().stream()
                .map(folga -> DayOfWeek.valueOf(folga.name()))
                .anyMatch(d -> d.equals(diaSemana));

        return !ehFolga; // true = trabalha, false = folga
    }
}
