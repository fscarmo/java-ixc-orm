package br.dev.fscarmo.ixcorm;


import br.dev.fscarmo.ixcorm.api.IxcRecordFactory;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;


/**
 * <p>
 * A classe 'IxcResponseBody' concentra a lógica de mapeamento dos dados recebidos no corpo da resposta HTTP da API do
 * IXC Provedor.
 * </p>
 *
 * @author Felipe S. Carmo
 * @version 1.1.0
 * @since 2025-09-28
 */
public class IxcResponseBody {

    private final JsonObject jsonObject;
    private String type;
    private String message;
    private int page;
    private int total;

    /**
     * @param jsonObject Um objeto JSON extraído do corpo de um 'HttpResponse', pela biblioteca Gson.
     */
    public IxcResponseBody(JsonObject jsonObject) {
        this.jsonObject = jsonObject;
        setupType();
        setupMessage();
        setupPage();
        setupTotal();
    }

    /**
     * @return Pode retornar "sucesso" ou "erro", dependendo do status de resposta da API do IXC Provedor.
     */
    public String getType() {
        return type;
    }

    /**
     * @return Pode retornar uma mensagem contendo informações sobre sucesso ou falha na requisição. Esse campo é
     *         nulo quando a resposta vem de uma requisição de listagem de registros.
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return O número da página atual, dos registros retornados pelo IXC Provedor.
     */
    public int getPage() {
        return page;
    }

    /**
     * @return A quantidade total de registros encontrados em uma consulta feita ao IXC Provedor.
     */
    public int getTotal() {
        return total;
    }

    /**
     * <p>
     * Procura pela propriedade "registros" no {@link JsonObject} da resposta e retorna como uma lista de objetos
     * mapeados pelo tipo da classe fornecido em <b>Class< T > mapper</b>. Quando a propriedade não é encontrada, a
     * função retorna uma lista vazia.
     * </p>
     *
     * <p>
     * O ideal é invocar <b>IxcResponseBody.getRegistros(Class<T> mapper)</b> apenas uma vez e armezenar o resultado em
     * uma variável do tipo <b>List< T ></b>, pois dependendo da quantidade de registros retornados por página,
     * a execução dessa função pode consumir muitos recursos.
     * </p>
     *
     * @param mapper A classe de um tipo genérico que herde de {@link IxcRecord}.
     * @return Uma lista de objetos do tipo "<b>T</b>".
     */
    public <T extends IxcRecord> List<T> getRecords(Class<T> mapper) {
        JsonArray jsonElements = jsonObject.getAsJsonArray("registros");
        if (jsonElements != null) {
            IxcRecordFactory<T> factory = new IxcRecordFactory<>(mapper);
            return jsonElements.asList().stream().map(factory::newRecord).toList();
        }
        return new ArrayList<>();
    }

    private void setupType() {
        JsonElement element = jsonObject.get("type");
        type = (element == null) ? "" : element.getAsString();
    }

    private void setupMessage() {
        JsonElement element = jsonObject.get("message");
        message = (element == null) ? "" : element.getAsString();
    }

    private void setupPage() {
        JsonElement element = jsonObject.get("page");
        page = (element == null) ? 0 : Integer.parseInt(element.getAsString());
    }

    private void setupTotal() {
        JsonElement element = jsonObject.get("total");
        total = (element == null) ? 0 : element.getAsInt();
    }
}
