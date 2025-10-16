package br.com.sistemacadastro.sistemacadastro.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import br.com.sistemacadastro.sistemacadastro.repository.TurnosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.sistemacadastro.sistemacadastro.model.Cargos;
import br.com.sistemacadastro.sistemacadastro.model.Colaborador;
import br.com.sistemacadastro.sistemacadastro.model.Contrato;
import br.com.sistemacadastro.sistemacadastro.model.Escalas;
import br.com.sistemacadastro.sistemacadastro.model.Setores;
import br.com.sistemacadastro.sistemacadastro.model.Turnos;
import br.com.sistemacadastro.sistemacadastro.repository.ColaboradorRepository;
import br.com.sistemacadastro.sistemacadastro.repository.EscalaRepository;
import br.com.sistemacadastro.sistemacadastro.repository.SetoresRepository;
import br.com.sistemacadastro.sistemacadastro.strategy.EscalaStrategyContext;

@Service
public class EscalaService {

    @Autowired
    private RegrasCLTService regrasCLTService;

    @Autowired
    private SetoresRepository setoresRepository;

    @Autowired
    private ColaboradorRepository colaboradorRepository;

    @Autowired
    private EscalaRepository escalaRepository;

    @Autowired
    private TurnosRepository turnosRepository;

    @Autowired
    private EscalaStrategyContext escalaStrategyContext;

    public List<Escalas> gerarEscalasSemanais() {
        List<Escalas> escalasGeradas = new ArrayList<>();
        List<Setores> setores = setoresRepository.findAll();
        LocalDate hoje = LocalDate.now();
        LocalDate segunda = hoje.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        for (Setores setor : setores) {
            List<Colaborador> colaboradores = colaboradorRepository.findBySetorAndContratoAtivo(setor.getId());

            for (Colaborador colaborador : colaboradores) {
                Contrato contrato = colaborador.getContrato();
                Cargos cargo = contrato.getCargos();
                List<Turnos> turnosDisponiveis = colaborador.getTurnos();

                if (turnosDisponiveis.isEmpty())
                    continue; // sem turno, pula colaborador

                for (int i = 0; i < 7; i++) {
                    LocalDate dataEscala = segunda.plusDays(i);
                    Date dataEscalaDate = Date.from(dataEscala.atStartOfDay(ZoneId.systemDefault()).toInstant());

                    boolean escalaExiste = escalaRepository
                            .existsByColaboradorIdAndDataEscala((long) colaborador.getId(), dataEscalaDate);

                    if (!escalaExiste) {
                        Escalas escala = new Escalas();

                        // ⚡ Corrigido: primeiro define colaborador e data para não dar NullPointerException
                        escala.setColaborador(colaborador);
                        escala.setDataEscala(dataEscalaDate);

                        // === Strategy aplicado para decidir se é folga ou escala ===
                        boolean trabalha = escalaStrategyContext.deveTrabalhar(colaborador, escala);
                        escala.setFolga(!trabalha);

                        escala.setSetores(setor);
                        escala.setStatusEscala(Escalas.StatusEscala.CRIADO);

                        Turnos turnoParaEscala = turnosDisponiveis.get(i % turnosDisponiveis.size());
                        escala.setTurnos(turnoParaEscala);

                        List<Escalas> escalasDaSemana = escalaRepository.findByColaboradorAndSemana(colaborador.getId(),
                                hoje);
                        List<Escalas> escalasParaValidar = new ArrayList<>(escalasDaSemana);
                        escalasParaValidar.add(escala);

                        if (regrasCLTService.podeEscalar(contrato, cargo, escalasParaValidar)) {
                            escalasGeradas.add(escalaRepository.save(escala));
                        }
                    }
                }
            }
        }

        return escalasGeradas;
    }

    public boolean alterarTurnoEscala(Integer colaboradorId, Date dataEscala, Turnos novoTurno) {
        LocalDate dataEscalaLocal = dataEscala.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        List<Escalas> escalas = escalaRepository.findByColaboradorAndSemana(colaboradorId, dataEscalaLocal);

        Escalas escalaExistente = escalas.stream()
                .filter(e -> {
                    LocalDate dataExistente = e.getDataEscala().toInstant().atZone(ZoneId.systemDefault())
                            .toLocalDate();
                    return dataExistente.isEqual(dataEscalaLocal);
                })
                .findFirst()
                .orElse(null);

        if (escalaExistente == null) {
            System.out.println("Escala não encontrada para colaborador " + colaboradorId + " na data " + dataEscala);
            return false;
        }

        if (novoTurno == null || novoTurno.getId() == 0) {
            // Caso folga - remover o turno da escala
            escalaExistente.setFolga(true);

        } else {
            // Caso turno normal - buscar no banco para garantir que existe
            Optional<Turnos> optionalTurno = turnosRepository.findById(novoTurno.getId());
            if (!optionalTurno.isPresent()) {
                System.out.println("Turno não encontrado no banco.");
                return false;
            }
            escalaExistente.setTurnos(optionalTurno.get());
            escalaExistente.setFolga(false);
        }

        escalaRepository.save(escalaExistente);
        System.out.println("Escala atualizada com sucesso.");
        return true;
    }

    public void revisarEscalasSemanaSetor(Integer setorId) {
        LocalDate hoje = LocalDate.now();
        LocalDate segunda = hoje.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate domingo = segunda.plusDays(6);

        Date dataInicio = Date.from(segunda.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date dataFim = Date.from(domingo.atStartOfDay(ZoneId.systemDefault()).toInstant());

        List<Escalas> escalasSemana = escalaRepository.findBySetoresIdAndDataEscalaBetween(setorId, dataInicio,
                dataFim);

        for (Escalas escala : escalasSemana) {
            escala.setStatusEscala(Escalas.StatusEscala.EM_ANALISE);
            escalaRepository.save(escala);
        }
    }

    public void aprovarEscalasSemanaSetor(Integer setorId) {
        LocalDate hoje = LocalDate.now();
        LocalDate segunda = hoje.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate domingo = segunda.plusDays(6);

        Date dataInicio = Date.from(segunda.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date dataFim = Date.from(domingo.atStartOfDay(ZoneId.systemDefault()).toInstant());

        List<Escalas> escalasSemana = escalaRepository.findBySetoresIdAndDataEscalaBetween(setorId, dataInicio,
                dataFim);

        for (Escalas escala : escalasSemana) {
            escala.setStatusEscala(Escalas.StatusEscala.PUBLICADO);
            escalaRepository.save(escala);
        }
    }
}
