package br.com.sistemacadastro.sistemacadastro.controller;

import br.com.sistemacadastro.sistemacadastro.dto.ColaboradorDTO;
import br.com.sistemacadastro.sistemacadastro.dto.EditDTO;
import br.com.sistemacadastro.sistemacadastro.model.Colaborador;
import br.com.sistemacadastro.sistemacadastro.model.Cargos;
import br.com.sistemacadastro.sistemacadastro.model.Contrato;
import br.com.sistemacadastro.sistemacadastro.model.Turnos;
import br.com.sistemacadastro.sistemacadastro.repository.CargoRepository;
import br.com.sistemacadastro.sistemacadastro.repository.EscalaRepository;
import br.com.sistemacadastro.sistemacadastro.repository.SolicitacoesRepository;
import br.com.sistemacadastro.sistemacadastro.repository.TurnosRepository;
import br.com.sistemacadastro.sistemacadastro.service.ColaboradorService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/colaborador")
public class ColaboradorController {

    @Autowired
    private ColaboradorService colaboradorService;

    @Autowired
    private CargoRepository cargoRepository;

    @Autowired
    private TurnosRepository turnosRepository;

    @Autowired
    private EscalaRepository escalaRepository;

    @Autowired
    private SolicitacoesRepository solicitacoesRepository;

    @GetMapping("/cadastrar")
    public String showCadastrarPage(Model model) {
        ColaboradorDTO colaboradorDto = new ColaboradorDTO();
        List<Cargos> cargos = cargoRepository.findAll();
        model.addAttribute("cargos", cargos);
        model.addAttribute("colaboradorDTO", colaboradorDto);
        List<Turnos> turnos = turnosRepository.findAll();
        model.addAttribute("turnos", turnos);
        model.addAttribute("diasSemana", Arrays.asList(Contrato.DiaFolga.values()));
        return "adminpages/cadastroColaborador";
    }

    @PostMapping("/cadastrar")
    public String cadastrarColaborador(@Valid @ModelAttribute ColaboradorDTO colaboradorDto, BindingResult result, Model model) {
        if (colaboradorDto.getTipoUsuario() == Colaborador.TipoUsuario.GERENTE) {
            result.rejectValue("tipoUsuario", "tipoUsuario.invalido", "Tipo de usuário 'GERENTE' não pode ser cadastrado.");
        }

        Optional<Colaborador> colaboradorExistente = colaboradorService.buscarPorEmail(colaboradorDto.getEmail());
        if (result.hasErrors()) {
            model.addAttribute("cargos", cargoRepository.findAll());
            model.addAttribute("turnos", turnosRepository.findAll());
            model.addAttribute("diasSemana", Arrays.asList(Contrato.DiaFolga.values()));
            model.addAttribute("colaboradorDTO", colaboradorDto);
            return "adminpages/cadastroColaborador";
        }

        if (colaboradorExistente.isEmpty()) {
            try {
                colaboradorService.salvarColaborador(colaboradorDto);
                return "redirect:/admin/main?sucesso=true";
            } catch (IllegalArgumentException ex) {
                // exemplo: CPF duplicado ou data inválida
                model.addAttribute("erroValidacao", ex.getMessage());
                model.addAttribute("cargos", cargoRepository.findAll());
                return "adminpages/cadastroColaborador";
            } catch (Exception ex) {
                model.addAttribute("erroGerar", true);
                model.addAttribute("cargos", cargoRepository.findAll());
                return "adminpages/cadastroColaborador";
            }
        } else {
            model.addAttribute("emailJaCadastrado", true);
            model.addAttribute("cargos", cargoRepository.findAll());
            return "adminpages/cadastroColaborador";
        }
    }

    @GetMapping("/editar/{id}")
    public String mostrarPagEdicao(Model model, @PathVariable("id") int id) {
        try {
            Colaborador colaborador = colaboradorService.buscarPorId(id);
            model.addAttribute("colaborador", colaborador);

            EditDTO editDto = new EditDTO();
            editDto.setId(colaborador.getId());
            editDto.setNome(colaborador.getNome());
            editDto.setEmail(colaborador.getEmail());
            editDto.setTelefone(colaborador.getTelefone());
            editDto.setCpf(colaborador.getCpf());
            editDto.setTipoUsuario(colaborador.getTipoUsuario());

            editDto.setEndereco(colaborador.getEndereco());
            editDto.setContrato(colaborador.getContrato());

            model.addAttribute("editDto", editDto);
        } catch (Exception ex) {
            System.out.println("Erro: " + ex.getMessage());
            return "redirect:/admin/main";
        }
        return "adminpages/EditColaborador";
    }

    @PostMapping("/editar")
    public String atualizarColaborador (Model model, @Valid @ModelAttribute EditDTO editDto, BindingResult result){
        if (result.hasErrors()) {
            return "adminpages/EditColaborador";
        }
        try {
            colaboradorService.atualizarColaborador(editDto);
        } catch (Exception ex) {
            System.out.println("Erro: " + ex.getMessage());
            // opcional: adicionar msg de erro no model
        }
        return "redirect:/admin/main?editado=true";
    }

    @GetMapping("/deletar")
    @Transactional
    public String excluirColaborador(@RequestParam int id) {
        try {
            Colaborador colaborador = colaboradorService.buscarPorId(id);
            if (colaborador != null) {
                boolean vinculadoAEscala = escalaRepository.existsByColaborador(colaborador);
                if (vinculadoAEscala) {
                    return "redirect:/admin/main?excluido=false&erro=restricao";
                }

                solicitacoesRepository.deleteByColaborador(colaborador);
                colaboradorService.excluirColaborador(colaborador); // implementar no service
                return "redirect:/admin/main?excluido=true";
            }
        } catch (DataIntegrityViolationException ex) {
            return "redirect:/admin/main?excluido=false&erro=restricao";
        } catch (Exception ex) {
            return "redirect:/admin/main?excluido=false&erro=geral";
        }
        return "redirect:/admin/main?excluido=false&erro=geral";
    }
}
