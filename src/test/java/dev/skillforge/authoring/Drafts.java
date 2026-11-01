package dev.skillforge.authoring;

import dev.skillforge.authoring.SkillDraft.Example;
import dev.skillforge.authoring.SkillDraft.ResourceKind;
import dev.skillforge.authoring.SkillDraft.ResourcePlan;
import dev.skillforge.authoring.SkillDraft.Step;

import java.util.List;

/** Rascunhos prontos para testes. */
public final class Drafts {

    private Drafts() {
    }

    public static SkillDraft minimal() {
        return new SkillDraft("revisao-de-codigo", "Revisa pull requests Java",
                List.of("o usuário pedir revisão de código"),
                null, null,
                List.of(new Step("Leia o diff", "Leia todas as mudanças antes de comentar qualquer coisa.")),
                null, null, null, null, null);
    }

    public static SkillDraft complete() {
        return new SkillDraft("revisao-de-codigo", "Revisa pull requests Java seguindo o checklist do time.",
                List.of("o usuário pedir revisão de código", "o usuário colar um diff"),
                List.of("o usuário pedir para escrever a funcionalidade do zero"),
                List.of("O diff ou link do pull request"),
                List.of(new Step("Entenda a intenção", "Leia a descrição do PR antes de opinar sobre o código."),
                        new Step("Percorra o checklist", "Verifique cada item do checklist e anote arquivo e linha.")),
                "Lista agrupada por prioridade: arquivo:linha — problema — sugestão.",
                List.of(new Example("revisa esse PR", "2 bloqueantes e 1 sugestão")),
                List.of("Comentar só estilo e esquecer bugs de lógica"),
                List.of("Todo comentário bloqueante explica o risco"),
                List.of(new ResourcePlan(ResourceKind.REFERENCE, "checklist", "Critérios de revisão do time"),
                        new ResourcePlan(ResourceKind.SCRIPT, "lint.sh", "Roda o linter no diff")));
    }

    public static SkillDraft with(java.util.function.UnaryOperator<SkillDraft> change) {
        return change.apply(complete());
    }
}
