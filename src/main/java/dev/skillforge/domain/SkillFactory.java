package dev.skillforge.domain;

/**
 * Converte {@link RawSkill} em {@link Skill}. Deve ser chamado só depois do lint sem erros;
 * se o nome ainda for inválido, {@link SkillName} lança {@link InvalidSkillNameException}.
 */
public final class SkillFactory {

    private SkillFactory() {
    }

    public static Skill fromRaw(RawSkill raw) {
        SkillMetadata metadata = new SkillMetadata(
                new SkillName(raw.name()),
                raw.description().strip(),
                raw.extraFrontmatter());
        return new Skill(metadata, raw.body(), raw.files(), raw.graph());
    }
}
