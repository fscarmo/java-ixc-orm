package br.dev.fscarmo.ixcorm;


import br.dev.fscarmo.ixcorm.api.RequestEmitter;
import br.dev.fscarmo.ixcorm.api.Parameter;
import br.dev.fscarmo.ixcorm.api.records.Ordering;
import br.dev.fscarmo.ixcorm.api.records.Pagination;
import br.dev.fscarmo.ixcorm.enums.Operator;
import br.dev.fscarmo.ixcorm.enums.Sort;

import java.util.ArrayList;
import java.util.List;


/**
 * <p>
 * A classe 'IxcOrm' fornece métodos que geram uma query de busca e herda o comportamento da classe {@link RequestEmitter},
 * para disponibilizar, através da mesma instância, os métodos que executam requisições HTTP, para a API do IXC
 * Provedor.
 * </p>
 *
 * <p>
 * Essa classe manipula as classes de ordenação, paginação e de construção de parâmetros, e gera um JSON compatível com
 * a query de busca da API do IXC Provedor.
 * </p>
 *
 * @author Felipe S. Carmo
 * @version 2.2.0
 * @since 2025-09-27
 */
public abstract class IxcOrm extends RequestEmitter {

    private final List<Parameter> parameters;
    private Ordering ordering;
    private Pagination pagination;
    private Parameter.Builder parameterBuilder;

    /**
     * <p>
     * 'IxcOrm' não pode ser instanciada, pois ela existe apenas para encapsular a lógica da requisição e da resposta
     * HTTP. A maneira correta de utilizá-la é através de herança, como no exemplo a seguir:
     * </p>
     *
     * {@snippet lang = java:
     * class Cliente extends IxcOrm {
     *
     *     private Cliente() {
     *         super("cliente");
     *     }
     *
     *     public void listaClientesPorNome(String nome) {
     *         IxcResponse response = new Cliente()
     *                .where("razao").like(nome)
     *                .GET();
     *         IO.println(response.getStatusCode());
     *     }
     * }
     *}
     * @param table O nome da tabela a ser consultada no IXC.
     */
    protected IxcOrm(String table) {
        super(table);
        parameters = new ArrayList<>();
        ordering = Ordering.ascBy(table, "id");
        pagination = Pagination.defaults();
        parameterBuilder = Parameter.newBuilder(table);
    }

    /**
     * Define a paginação na query de consulta.
     *
     * @param pagination Um objeto {@link Pagination} com as configurações de paginação.
     * @return A própria instância de {@link IxcOrm}.
     */
    public IxcOrm setPagination(Pagination pagination) {
        this.pagination = pagination;
        return this;
    }

    /**
     * <p>
     * Inicia um novo objeto de parâmetro para a propriedade <b>grid_param</b> da query.
     * </p>
     *
     * @param column O campo da tabela que será usado como filtro na busca.
     * @return A própria instância de {@link IxcOrm}.
     */
    public IxcOrm where(String column) {
        parameterBuilder.type(column);
        return this;
    }

    /**
     * <p>
     * Adiciona o operador de comparação (L) e o valor a ser filtrado, no objeto de parâmetro, iniciado por
     * <b>where(String column)</b>.
     * </p>
     *
     * @param value O valor do campo da tabela que será usado como filtro na busca.
     * @return A própria instância de {@link IxcOrm}.
     */
    public IxcOrm like(Object value) {
        parameterBuilder.operator(Operator.LIKE);
        parameterBuilder.value(value);
        addParamToGridAndReset();
        setQuery(getQueryAsJson());
        return this;
    }

    /**
     * <p>
     * Adiciona o operador de comparação (=) e o valor a ser filtrado, no objeto de parâmetro, iniciado por
     * <b>where(String column)</b>.
     * </p>
     *
     * @param value O valor do campo da tabela que será usado como filtro na busca.
     * @return A própria instância de {@link IxcOrm}.
     */
    public IxcOrm exactly(Object value) {
        parameterBuilder.operator(Operator.EQUALS);
        parameterBuilder.value(value);
        addParamToGridAndReset();
        setQuery(getQueryAsJson());
        return this;
    }

    /**
     * <p>
     * Adiciona o operador de comparação (!=) e o valor a ser filtrado, no objeto de parâmetro, iniciado por
     * <b>where(String column)</b>.
     * </p>
     *
     * @param value O valor do campo da tabela que será usado como filtro na busca.
     * @return A própria instância de {@link IxcOrm}.
     */
    public IxcOrm not(Object value) {
        parameterBuilder.operator(Operator.NOT);
        parameterBuilder.value(value);
        addParamToGridAndReset();
        setQuery(getQueryAsJson());
        return this;
    }

    /**
     * <p>
     * Adiciona o operador de comparação (<) e o valor a ser filtrado, no objeto de parâmetro, iniciado por
     * <b>where(String column)</b>.
     * </p>
     *
     * @param value O valor do campo da tabela que será usado como filtro na busca.
     * @return A própria instância de {@link IxcOrm}.
     */
    public IxcOrm lessThan(Object value) {
        parameterBuilder.operator(Operator.LESS_THAN);
        parameterBuilder.value(value);
        addParamToGridAndReset();
        setQuery(getQueryAsJson());
        return this;
    }

    /**
     * <p>
     * Adiciona o operador de comparação (<=) e o valor a ser filtrado, no objeto de parâmetro, iniciado por
     * <b>where(String column)</b>.
     * </p>
     *
     * @param value O valor do campo da tabela que será usado como filtro na busca.
     * @return A própria instância de {@link IxcOrm}.
     */
    public IxcOrm lessThanEquals(Object value) {
        parameterBuilder.operator(Operator.LESS_THAN_EQUALS);
        parameterBuilder.value(value);
        addParamToGridAndReset();
        setQuery(getQueryAsJson());
        return this;
    }

    /**
     * <p>
     * Adiciona o operador de comparação (>) e o valor a ser filtrado, no objeto de parâmetro, iniciado por
     * <b>where(String column)</b>.
     * </p>
     *
     * @param value O valor do campo da tabela que será usado como filtro na busca.
     * @return A própria instância de {@link IxcOrm}.
     */
    public IxcOrm greaterThan(Object value) {
        parameterBuilder.operator(Operator.GREATER_THAN);
        parameterBuilder.value(value);
        addParamToGridAndReset();
        setQuery(getQueryAsJson());
        return this;
    }

    /**
     * <p>
     * Adiciona o operador de comparação (>=) e o valor a ser filtrado, no objeto de parâmetro, iniciado por
     * <b>where(String column)</b>.
     * </p>
     *
     * @param value O valor do campo da tabela que será usado como filtro na busca.
     * @return A própria instância de {@link IxcOrm}.
     */
    public IxcOrm greaterThanEquals(Object value) {
        parameterBuilder.operator(Operator.GREATER_THAN_EQUALS);
        parameterBuilder.value(value);
        addParamToGridAndReset();
        setQuery(getQueryAsJson());
        return this;
    }

    /**
     * <p>
     * Define como a API do IXC Provedor deverá ordenar os dados retornadaos pela busca.
     * </p>
     *
     * @param order O tipo de ordenação (asc | desc).
     * @param column O Campo da tabela que será usado para ordenar os registros retornados.
     * @return A própria instância de {@link IxcOrm}.
     */
    public IxcOrm orderBy(Sort order, String column) {
        ordering = new Ordering(column, order);
        setQuery(getQueryAsJson());
        return this;
    }

    /**
     * <p>
     * Concatena as propriedades da query e a propriedade <b>grid_param</b> e os retorna em um único JSON, no
     * seguinte formato:
     * </p>
     *
     * {@snippet lang=json:
     * {
     *     "qtype": "cliente",
     *     "query": "",
     *     "oper": "",
     *     "page": "1",
     *     "rg": 20,
     *     "sortname": "asc",
     *     "sortorder": "cliente.id",
     *     "grid_param": [
     *         {
     *             "TB": "cliente.razao",
     *             "OP": "L",
     *             "P": "nome do cliente (nesse caso)"
     *         }
     *     ]
     * }
     * }
     * @return Uma {@link String} no formato JSON.
     */
    protected String getQueryAsJson() {
        String jsonQueryProps = getQueryPropsAsJson();
        String jsonGridParams = getGridParamsAsJson();
        return "{"+ jsonQueryProps +","+ jsonGridParams +"}";
    }

    private void addParamToGridAndReset() {
        String table = getTable();
        parameters.add(parameterBuilder.build());
        parameterBuilder = Parameter.newBuilder(table);
    }

    private String getQueryPropsAsJson() {
        return "\"qtype\":\""+ getTable() +"\"," +
               "\"query\":\"\"," +
               "\"oper\":\"\"," +
               "\"page\":"+ pagination.page() +"," +
               "\"rp\":"+ pagination.rows() +"," +
               "\"sortname\":\""+ ordering.sortName() +"\"," +
               "\"sortorder\":\""+ ordering.sortOrder().value() +"\"";
    }

    private String getGridParamsAsJson() {
        StringBuilder builded = new StringBuilder().append("\"grid_param\":\"[");
        for (Parameter param : parameters) {
            builded.append(param.toString()).append(",");
        }
        return builded.append("]").toString().replace(",]", "]\"");
    }
}
