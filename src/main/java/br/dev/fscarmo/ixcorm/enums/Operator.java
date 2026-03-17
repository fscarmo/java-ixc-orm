package br.dev.fscarmo.ixcorm.enums;


/**
 * <p>
 * O enum 'Operator' serve como um wrapper para os operadores de comparação que poderão ser urilizados para realizar
 * buscas na API do IXC Provedor.
 * </p>
 *
 * @author Felipe S. Carmo
 * @version 1.1.0
 * @since 2025-09-28
 */
public enum Operator {

    EQUALS("="),
    NOT("!="),
    LIKE("L"),
    LESS_THAN("<"),
    LESS_THAN_EQUALS("<="),
    GREATER_THAN(">"),
    GREATER_THAN_EQUALS(">=");

    private final String value;

    Operator(String operator) {
        value = operator;
    }

    public String value() {
        return value;
    }
}
