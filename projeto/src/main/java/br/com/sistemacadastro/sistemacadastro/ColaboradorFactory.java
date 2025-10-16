package br.com.sistemacadastro.sistemacadastro;

import br.com.sistemacadastro.sistemacadastro.dto.ColaboradorDTO;
import br.com.sistemacadastro.sistemacadastro.model.*;
import br.com.sistemacadastro.sistemacadastro.repository.CargoRepository;
import br.com.sistemacadastro.sistemacadastro.repository.EnderecoRepository;
import br.com.sistemacadastro.sistemacadastro.repository.TurnosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ColaboradorFactory {

    @Autowired
    private EnderecoRepository enderecoRepository;

    @Autowired
    private CargoRepository cargoRepository;

    @Autowired
    private TurnosRepository turnosRepository;

    public Colaborador criarColaborador(ColaboradorDTO dto) {
        Colaborador colaborador = new Colaborador();
        colaborador.setNome(dto.getNome());
        colaborador.setEmail(dto.getEmail());
        colaborador.setSenha(dto.getSenha());
        colaborador.setTipoUsuario(dto.getTipoUsuario());
        colaborador.setTelefone(dto.getTelefone());
        colaborador.setCpf(dto.getCpf());
        colaborador.setDataNascimento(dto.getDataNascimento());

        Endereco endereco = enderecoRepository.save(dto.getEndereco());
        colaborador.setEndereco(endereco);

        Contrato contrato = dto.getContrato();
        contrato.setColaborador(colaborador);
        contrato.setDiasFolga(dto.getDiasFolga());

        Cargos cargo = cargoRepository.findById(dto.getCargoId())
                .orElseThrow(() -> new RuntimeException("Cargo não encontrado"));
        contrato.setCargos(cargo);

        colaborador.setContrato(contrato);

        List<Turnos> turnos = turnosRepository.findAllById(dto.getTurnosIds());
        colaborador.setTurnos(turnos);

        return colaborador;
    }
}
