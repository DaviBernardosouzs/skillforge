---
title: Workflow com script
description: Executa um script da pasta scripts/ para fazer uma tarefa determinística e interpreta o resultado.
---
# {{title}}

{{description}}

## Quando usar
- A tarefa é repetitiva e o resultado precisa ser sempre igual (conversão, validação, extração).
- O usuário pede para rodar o processo automatizado desta skill.

## Entradas
- Os arquivos ou parâmetros que o script recebe.

## Processo
1. **Confira as entradas** — verifique se os arquivos existem e estão no formato esperado antes de rodar.
2. **Rode o script** — execute `scripts/run.sh` passando as entradas; não reescreva a lógica dele à mão.
3. **Interprete a saída** — resuma o resultado para o usuário e destaque erros ou avisos do script.

## Recursos
- `scripts/run.sh` — script principal. Crie esse arquivo na pasta `scripts/` da skill.

## Formato da saída
Um resumo curto do que o script fez, seguido dos erros ou avisos encontrados.

## Verificação antes de entregar
- [ ] O script terminou com código 0, ou o erro foi explicado.
