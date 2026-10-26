package br.com.ada.lms.servico;

import br.com.ada.lms.excecao.RequisicaoInvalidaException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import java.util.stream.Collectors;

/**
 * No PATCH o cliente envia só os campos que quer alterar, então não dá para usar @Valid
 * (o @NotBlank reclamaria dos campos ausentes). Aqui validamos apenas os campos
 * que vieram preenchidos: violações cujo valor é null (campo não enviado) são ignoradas.
 */
@ApplicationScoped
public class ValidadorPatch {

    private final Validator validator;

    public ValidadorPatch(Validator validator) {
        this.validator = validator;
    }

    public void validarCamposInformados(Object request) {
        if (request == null) {
            throw new RequisicaoInvalidaException("corpo da requisição é obrigatório");
        }
        String erros = validator.validate(request).stream()
                .filter(violacao -> violacao.getInvalidValue() != null)
                .map(ConstraintViolation::getMessage)
                .sorted()
                .collect(Collectors.joining("; "));

        if (!erros.isEmpty()) {
            throw new RequisicaoInvalidaException(erros);
        }
    }
}
