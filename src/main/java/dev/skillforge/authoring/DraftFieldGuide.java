package dev.skillforge.authoring;

import java.util.List;

/**
 * Descrição dos campos do {@link SkillDraft} para um front montar o formulário guiado
 * sem precisar escrever as perguntas no código dele.
 */
public final class DraftFieldGuide {

    /** Formato de cada campo, para o front escolher o componente. */
    public enum FieldType { TEXT, TEXTAREA, TEXT_LIST, STEP_LIST, EXAMPLE_LIST, RESOURCE_LIST }

    public record Field(String name, String label, boolean required, FieldType type,
                        String question, String example, String hint) {
    }

    public static final List<Field> FIELDS = List.of(
            new Field("name", "Nome", true, FieldType.TEXT,
                    "Como a skill vai se chamar?", "revisao-de-codigo",
                    "kebab-case: letras minúsculas, números e hífens. Vira o nome da pasta."),
            new Field("purpose", "Propósito", true, FieldType.TEXTAREA,
                    "O que a skill faz, em uma frase?", "Revisa pull requests Java seguindo o checklist do time.",
                    "Começa a description. Seja específico sobre o resultado."),
            new Field("triggers", "Quando usar", true, FieldType.TEXT_LIST,
                    "Em que situações o agente deve usar esta skill?", "o usuário pedir revisão de código",
                    "Complete a frase \"Use quando...\". Liste pelo menos duas situações."),
            new Field("antiTriggers", "Quando NÃO usar", false, FieldType.TEXT_LIST,
                    "Em que pedidos parecidos a skill NÃO deve ser usada?", "o usuário pedir para escrever a funcionalidade do zero",
                    "Evita que a skill seja acionada por engano."),
            new Field("inputs", "Entradas", false, FieldType.TEXT_LIST,
                    "O que a skill precisa receber do usuário ou do contexto?", "O diff ou link do pull request",
                    null),
            new Field("steps", "Processo", true, FieldType.STEP_LIST,
                    "Quais são os passos do trabalho, em ordem?", "Percorra o checklist — verifique cada item e anote arquivo e linha.",
                    "Cada passo tem um título curto e um detalhe com o que fazer e por quê (5 palavras ou mais)."),
            new Field("outputFormat", "Formato da saída", false, FieldType.TEXTAREA,
                    "Como deve ser a entrega final?", "Lista agrupada por prioridade: arquivo:linha — problema — sugestão.",
                    null),
            new Field("examples", "Exemplos", false, FieldType.EXAMPLE_LIST,
                    "Tem um exemplo de pedido e da resposta esperada?", "Entrada: \"revisa esse PR\" → Saída: 2 bloqueantes, 1 sugestão",
                    "Exemplos concretos ensinam mais que regras."),
            new Field("pitfalls", "Armadilhas", false, FieldType.TEXT_LIST,
                    "Que erros o agente costuma cometer nessa tarefa?", "Comentar estilo e esquecer bugs de lógica",
                    null),
            new Field("qualityChecks", "Verificação antes de entregar", false, FieldType.TEXT_LIST,
                    "O que conferir antes de responder?", "Todo comentário bloqueante explica o risco",
                    null),
            new Field("resources", "Recursos", false, FieldType.RESOURCE_LIST,
                    "A skill precisa de scripts, referências ou arquivos de apoio?", "REFERENCE · checklist.md · critérios de revisão",
                    "SCRIPT vai para scripts/, REFERENCE para references/, ASSET para assets/."));

    private DraftFieldGuide() {
    }
}
