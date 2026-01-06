package com.fatec.assistentej.service;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fatec.assistentej.model.AvaliacaoResposta;
import com.fatec.assistentej.model.RagRespostaAvaliacaoRequest;

import dev.langchain4j.model.chat.ChatLanguageModel;

/**
 * responde - Se a pergunta foi respondida (true) ou recusou/não encontrou?
 * (false).
 * nota (Qualidade / Nuance) - De 0 a 10, quão bem escrita, completa e útil é a
 * resposta? (Filtro de qualidade).
 * fidelidade (Dector de mentiras) - boolean A resposta se baseia apenas no
 * contexto fornecido?
 * comentario (Justificativa / Debug) - Explicação textual do porquê da nota.
 * Essencial para auditoria.
 */
@Service
public class LLMJudgeService {

    private final ChatLanguageModel chatModel;
    private final ObjectMapper mapper = new ObjectMapper();

    public LLMJudgeService(ChatLanguageModel chatModel) {
        this.chatModel = chatModel;
    }

    public AvaliacaoResposta avaliarRagResposta(RagRespostaAvaliacaoRequest req) {
        String prompt = """
                Você é um juiz especialista em avaliar sistemas RAG.
                Sua tarefa é validar se a resposta gerada respeita ESTRITAMENTE o contexto fornecido.
                Não deixe seu conhecimento prévio influenciar a validação da fidelidade.

                Dados da execução:
                -------------------
                [Pergunta do Usuário]:
                "%s"

                [Contexto Recuperado (Fonte da Verdade)]:
                "%s"

                [Resposta do Sistema]:
                "%s"
                -------------------

                Critérios de Avaliação:
                1. Fidelidade: A resposta deve ser derivada 100%% do contexto. Se a resposta contiver informações corretas no mundo real mas que NÃO estão no texto do contexto, a fidelidade é FALSE.
                2. Qualidade (Nota): Avalie a clareza e utilidade. Penalize alucinações (infos fora do contexto).

                Saída esperada (Responda APENAS o JSON bruto):
                {
                    "responde": (boolean - A resposta tenta responder à pergunta? Se recusou ou disse que não sabe, use false),
                    "nota": (int - 0 a 10. Se houver alucinação fora do contexto, a nota máxima deve ser 8),
                    "fidelidade": (boolean - É false se houver QUALQUER informação não presente no contexto),
                    "comentario": "(string - Identifique explicitamente frase por frase o que está no contexto e o que foi alucinado/inventado)"
                }
                """
                .formatted(req.getPergunta(), req.getContextoRag(), req.getResposta());

        try {
            String respostaJson = chatModel.generate(prompt);
            // Sanitize response to remove potential markdown code blocks
            respostaJson = respostaJson.replace("```json", "").replace("```", "").trim();
            return mapper.readValue(respostaJson, AvaliacaoResposta.class);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao avaliar resposta via LLM juiz", e);
        }
    }

}
