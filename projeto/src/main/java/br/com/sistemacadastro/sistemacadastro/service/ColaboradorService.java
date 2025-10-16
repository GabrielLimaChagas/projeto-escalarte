package br.com.sistemacadastro.sistemacadastro.service;

import br.com.sistemacadastro.sistemacadastro.dto.ColaboradorDTO;
import br.com.sistemacadastro.sistemacadastro.dto.EditDTO;
import br.com.sistemacadastro.sistemacadastro.ColaboradorFactory;
import br.com.sistemacadastro.sistemacadastro.model.Colaborador;
import br.com.sistemacadastro.sistemacadastro.repository.ColaboradorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ColaboradorService {

    @Autowired
    private ColaboradorRepository colaboradorRepository;

    @Autowired
    private ColaboradorFactory colaboradorFactory;

    public Optional<Colaborador> buscarPorEmail(String email) {
        return colaboradorRepository.findByEmail(email);
    }

    public void salvarColaborador(ColaboradorDTO colaboradorDto) {
        // 🧩 Validações
        if (colaboradorRepository.existsByCpf(colaboradorDto.getCpf())) {
            throw new IllegalArgumentException("Este CPF já está cadastrado");
        }
        if (colaboradorDto.getDataNascimento().isAfter(java.time.LocalDate.now())) {
            throw new IllegalArgumentException("Data de nascimento não pode ser no futuro");
        }

        // 🏭 Usa a Factory para criar o colaborador
        Colaborador colaborador = colaboradorFactory.criarColaborador(colaboradorDto);

        // 🔹 Salva no banco
        colaboradorRepository.save(colaborador);
    }

    public Colaborador buscarPorId(int id) {
        Colaborador colaborador = colaboradorRepository.findById(id);
        if (colaborador == null) {
            throw new RuntimeException("Colaborador não encontrado");
        }
        return colaborador;
    }

    public void atualizarColaborador(EditDTO editDto) {
        Colaborador colaborador = colaboradorRepository.findById(editDto.getId());

        colaborador.setNome(editDto.getNome());
        colaborador.setEmail(editDto.getEmail());
        colaborador.setTelefone(editDto.getTelefone());
        colaborador.setCpf(editDto.getCpf());
        colaborador.setTipoUsuario(editDto.getTipoUsuario());

        colaborador.getEndereco().setBairro(editDto.getEndereco().getBairro());
        colaborador.getEndereco().setRua(editDto.getEndereco().getRua());
        colaborador.getEndereco().setCep(editDto.getEndereco().getCep());
        colaborador.getEndereco().setNumero(editDto.getEndereco().getNumero());

        colaborador.getContrato().setAtivo(editDto.getContrato().isAtivo());

        colaboradorRepository.save(colaborador);
    }

    public void excluirColaborador(Colaborador colaborador) {
        colaboradorRepository.delete(colaborador);
    }

}
