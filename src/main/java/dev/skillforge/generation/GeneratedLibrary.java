package dev.skillforge.generation;

import dev.skillforge.domain.LintReport;

/** Resultado da geração: o zip pronto e o relatório (que pode ter avisos). */
public record GeneratedLibrary(String fileName, byte[] zip, LintReport report) {
}
