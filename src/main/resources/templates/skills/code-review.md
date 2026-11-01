---
title: Revisão de código
description: Revisa mudanças de código seguindo um checklist fixo e devolve comentários priorizados.
---
# {{title}}

{{description}}

## Quando usar
- O usuário pede revisão de um PR, diff ou arquivo.
- O usuário quer saber se uma mudança está pronta para merge.

## Quando NÃO usar
- O usuário quer que você escreva a funcionalidade do zero, não revisar.

## Processo
1. **Entenda a intenção** — leia a descrição do PR ou pergunte o objetivo antes de opinar.
2. **Percorra o checklist** — verifique cada item abaixo e anote onde ele falha, com arquivo e linha.
3. **Priorize** — classifique cada comentário como bloqueante, importante ou sugestão.

## Checklist
- Nomes de classes, métodos e variáveis dizem o que fazem.
- Casos de erro e entradas vazias são tratados.
- Há testes cobrindo o comportamento novo.
- Nenhum segredo, senha ou token no código.
- Sem código morto ou comentado.

## Formato da saída
Uma lista agrupada por prioridade. Cada item: `arquivo:linha — problema — sugestão`.

## Verificação antes de entregar
- [ ] Todo comentário bloqueante explica o risco.
- [ ] Nenhum item do checklist ficou sem ser verificado.
