package br.dev.fscarmo.ixcorm.api;


import br.dev.fscarmo.ixcorm.IxcContext;
import br.dev.fscarmo.ixcorm.IxcRecord;
import br.dev.fscarmo.ixcorm.IxcResponse;
import br.dev.fscarmo.ixcorm.api.records.Header;
import br.dev.fscarmo.ixcorm.enums.Method;
import br.dev.fscarmo.ixcorm.exception.NetworkConnectionException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;


/**
 * <p>
 * A classe 'RequestEmitter' manipula a query de busca, constrói o manipulador de requisições HTTP e fornece acesso aos
 * métodos de requisição de forma padronizada.
 * </p>
 *
 * @author Felipe S. Carmo
 * @version 2.0.1
 * @since 2025-09-27
 */
public abstract class RequestEmitter {

    private final List<Header> headers = new ArrayList<>();
    private final String table;
    private HttpRequest.BodyPublisher publisher;
    private String query;
    private URI uri;

    /**
     * @param table Representa o endpoint do IXC Provedor para o qual a requisição será enviada.
     */
    protected RequestEmitter(String table) {
        this.table = table;
        setupDefaultHeaders();
    }

    /**
     * <p>
     * Envia uma requisição HTTP para a API do IXC Provedor, para listar registros, filtrando-os pela query de busca
     * definida por <b>setQuery(String query).</b>
     * A requisição é do tipo POST, o que define que ela irá executar uma listagem de registros é a presença do header:
     * ["ixcsoft": "listar"].
     * </p>
     *
     * @return Um objeto {@link IxcResponse}.
     * @throws NetworkConnectionException Se ocorrer alguma falha na comunicação com o IXC Provedor.
     */
    public IxcResponse GET() throws NetworkConnectionException {
        setupUri();
        enableIxcListingHeader();
        setupBodyPublisher(query);
        HttpResponse<String> response = emitRequest(Method.POST);
        return new IxcResponse(response);
    }

    /**
     * <p>
     * Envia uma requisição HTTP para a API do IXC Provedor, para inserir um novo registro no banco de dados, na tabela
     * definida pelo prâmetro <b>(String table)</b> no construtor.
     * </p>
     *
     * @param record O novo registro a ser inserido no banco de dados.
     * @return Um objeto {@link IxcResponse} contento o status e uma mensagem com a informação sobre o resultado da
     * requisição.
     * @throws NetworkConnectionException Se ocorrer alguma falha na comunicação com o IXC Provedor.
     */
    public IxcResponse POST(IxcRecord record) throws NetworkConnectionException {
        setupUri(record.getId());
        disableIxcListingHeader();
        setupBodyPublisher(record.toJsonString());
        HttpResponse<String> response = emitRequest(Method.POST);
        return new IxcResponse(response);
    }

    /**
     * <p>
     * Envia uma requisição HTTP para a API do IXC Provedor, para atualizar um ou mais campos de um registro no banco
     * de dados, na tabela definida pelo prâmetro <b>(String table)</b> no construtor.
     * </p>
     *
     * @param record O registro com os campos a serem atualizados no banco de dados.
     * @return Um objeto {@link IxcResponse} contento o status da requisição e uma mensagem que pode ser de sucesso ou
     * de erro, dependendo do status.
     * @throws NetworkConnectionException Se ocorrer alguma falha na comunicação com o IXC Provedor.
     */
    public IxcResponse PUT(IxcRecord record) throws NetworkConnectionException {
        setupUri(record.getId());
        disableIxcListingHeader();
        setupBodyPublisher(record.toJsonString());
        HttpResponse<String> response = emitRequest(Method.PUT);
        return new IxcResponse(response);
    }

    /**
     * <p>
     * Envia uma requisição HTTP para a API do IXC Provedor, para excluir um determinado registro do banco de dados.
     * </p>
     *
     * @param id Um {@link Integer} com o id do registro a ser removido do banco de dados do IXC Provedor.
     * @return Um objeto {@link IxcResponse}.
     * @throws NetworkConnectionException Se ocorrer alguma falha na comunicação com o IXC Provedor.
     */
    public IxcResponse DELETE(Integer id) throws NetworkConnectionException {
        setupUri(id);
        disableIxcListingHeader();
        setupBodyPublisher(null);
        HttpResponse<String> response = emitRequest(Method.DELETE);
        return new IxcResponse(response);
    }

    /**
     * <p>
     * Obtém a "tabela" definida no contrutor da classe.
     * </p>
     *
     * @return Uma {@link String} com o endpoint para o qual as requisições serão enviadas.
     */
    protected String getTable() {
        return table;
    }

    /**
     * <p>
     * Define a query de busca que será enviada para a API do IXC Provedor.
     * </p>
     *
     * @param query Uma {@link String} JSON com o corpo da query no formato exigido pela API do IXC Provedor.
     */
    protected void setQuery(String query) {
        this.query = query;
    }

    /**
     * <p>
     * Envia a requisição para a API do IXC Provedor e retorna o coteúdo em um string.
     * </p>
     *
     * @param method GET, POST, PUT, DELETE
     * @return O conteúdo da resposta em String
     * @throws NetworkConnectionException
     */
    protected HttpResponse<String> emitRequest(Method method) throws NetworkConnectionException {
        try (HttpClient client = HttpClient.newHttpClient()) {

            HttpRequest.Builder builder = HttpRequest.newBuilder(uri);
            builder.method(method.value(), publisher);
            headers.forEach(h -> builder.setHeader(h.getName(), h.getValue()));

            return client.send(
                    builder.build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );
        }
        catch (IllegalArgumentException | UncheckedIOException | InterruptedException | IOException e) {
            throw new NetworkConnectionException();
        }
    }

    private void setupDefaultHeaders() {
        String encodedToken = getEncodedTokenFromContext();
        headers.add(Header.of("Authorization", "Basic "+ encodedToken));
        headers.add(Header.of("Content-Type", "application/json"));
        headers.add(Header.of("ixcsoft", ""));
    }

    private String getEncodedTokenFromContext() {
        String tokenFromEnv = IxcContext.INSTANCE.getEnv().getToken();
        byte[] bytes = tokenFromEnv.getBytes(StandardCharsets.UTF_8);
        return Base64.getEncoder().encodeToString(bytes);
    }

    private void setupUri() {
        String domain = IxcContext.INSTANCE.getEnv().getDomain();
        uri = URI.create("https://"+ domain +"/webservice/v1/"+ table);
    }

    private void setupUri(Integer id) {
        String domain = IxcContext.INSTANCE.getEnv().getDomain();
        uri = URI.create("https://"+ domain +"/webservice/v1/"+ table +"/"+ id);
    }

    private void enableIxcListingHeader() {
        headers.stream()
                .filter(h -> h.hasName("ixcsoft"))
                .findFirst().ifPresent(h -> h.setValue("listar"));
    }

    private void disableIxcListingHeader() {
        headers.stream()
                .filter(h -> h.hasName("ixcsoft"))
                .findFirst().ifPresent(h -> h.setValue(""));
    }

    private void setupBodyPublisher(String body) {
        boolean isValidBody = (body != null && !body.isBlank());
        publisher = (isValidBody)
                ? HttpRequest.BodyPublishers.ofString(body)
                : HttpRequest.BodyPublishers.noBody();
    }
}
