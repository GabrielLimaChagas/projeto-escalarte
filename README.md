# ProjetoEscarlarte

Desenvolvedores: Nicolas Hara Tancon, Lincoln Brunkow Neto, Thales Henrique Calil, Gabriel de Lima Chagas e Caua Kammler Pacheco Rodrigues

O projeto Escalarte nasceu da necessidade de resolver um problema comum enfrentado por microempresas: a dificuldade na gestão de escalas de trabalho dos colaboradores. Muitas vezes, esses negócios lidam com conflitos de horários, mudanças de última hora e falta de organização no processo de criação de escalas. Além disso, o controle é frequentemente feito de forma manual, o que gera retrabalho e perda de tempo.

Pensando nisso, desenvolvemos o Escalarte, um sistema voltado para facilitar e automatizar o gerenciamento de escalas de forma prática e intuitiva. Nosso público-alvo são empresários que buscam agilidade e eficiência no processo de escalonamento de suas equipes.

O objetivo principal do Escalarte é gerar escalas rapidamente, garantindo que não haja sobreposição de horários entre os funcionários e permitindo que os próprios colaboradores possam solicitar alterações em suas escalas, tornando o processo mais democrático e flexível. O grande valor que entregamos é transformar uma tarefa cansativa e estressante em algo simples, rápido e eficaz.

## Setup
Use XAMPP para o Banco de Dados.
```sql
/* Criar banco de dados */
CREATE DATABASE db_escalarte;
USE db_escalarte;

/* Criar usuário admin */
INSERT INTO colaborador (
    nome,
    email,
    senha,
    tipo_usuario,
    cpf,
    telefone,
    data_nascimento
) VALUES (
    'Admin User',
    'admin@admin.com',
    '1234',
    0,
    '12345678909',
    '(11) 99999-9999',
    '1990-01-01'
);
```