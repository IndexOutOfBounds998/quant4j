package com.qklx.qt.core.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.qklx.qt.common.config.VpnProxyConfig;
import com.qklx.qt.core.request.CreateOrderRequest;
import com.qklx.qt.core.request.DepthRequest;
import com.qklx.qt.core.request.IntrustOrdersDetailRequest;
import com.qklx.qt.core.response.*;
import okhttp3.*;
import okhttp3.OkHttpClient.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * API client.
 *
 * @Date 2018/1/14
 * @Time 16:02
 */

public class ApiClient {

    /**
     * 定义超时时间
     */
    private static final int CONN_TIMEOUT = 5;
    private static final int READ_TIMEOUT = 5;
    private static final int WRITE_TIMEOUT = 5;

    private static final String API_HOST = "api.huobi.pro";

    private static final String API_URL = "https://" + API_HOST;
    private static final MediaType JSON = MediaType.parse("application/json");
    private OkHttpClient client;

    private String accessKeyId;
    private String accessKeySecret;
    private String assetPassword;

    /**
     * 创建一个ApiClient实例
     *
     * @param accessKeyId     AccessKeyId
     * @param accessKeySecret AccessKeySecret
     */
    public ApiClient(String accessKeyId, String accessKeySecret, VpnProxyConfig vpnProxyConfig) {
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.assetPassword = null;
        if (vpnProxyConfig.getEnable()) {
            client = createOkHttpClient(vpnProxyConfig.getIp(), vpnProxyConfig.getPort());
        } else {
            client = createOkHttpClient();
        }

    }

    public ApiClient() {
    }

    public ApiClient(VpnProxyConfig vpnProxyConfig) {
        if (vpnProxyConfig.getEnable()) {
            client = createOkHttpClient(vpnProxyConfig.getIp(), vpnProxyConfig.getPort());
        } else {
            client = createOkHttpClient();
        }
    }

    /**
     * 创建一个ApiClient实例
     *
     * @param accessKeyId     AccessKeyId
     * @param accessKeySecret AccessKeySecret
     * @param assetPassword   AssetPassword
     */
    public ApiClient(String accessKeyId, String accessKeySecret, String assetPassword) {
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.assetPassword = assetPassword;
    }

    /**
     * 查询交易对
     *
     * @return List of symbols.
     */
    public List<Symbol> getSymbols() {
        ApiResponse<List<Symbol>> resp =
                get("/v1/common/symbols", null, new TypeReference<ApiResponse<List<Symbol>>>() {
                });
        return resp.checkAndReturn();
    }

    /**
     * 查询所有账户信息
     *
     * @return List of accounts.
     */
    public List<Account> getAccounts() {
        ApiResponse<List<Account>> resp =
                get("/v1/account/accounts", null, new TypeReference<ApiResponse<List<Account>>>() {
                });
        return resp.checkAndReturn();
    }

    /**
     * 创建订单
     *
     * @param request CreateOrderRequest object.
     * @return Order id.
     */
    public Long createOrder(CreateOrderRequest request) {
        ApiResponse<Long> resp =
                post("/v1/order/orders/place", request, new TypeReference<ApiResponse<Long>>() {
                });
        return resp.checkAndReturn();
    }

    /**
     * 执行订单
     *
     * @param orderId The id of created order.
     * @return Order id.
     */
    public String placeOrder(long orderId) {
        ApiResponse<String> resp = post("/v1/order/orders/" + orderId + "/place", null,
                new TypeReference<ApiResponse<String>>() {
                });
        return resp.checkAndReturn();
    }

    /**
     * 查询当前未成交订单
     * 查询已提交但是仍未完全成交或未被撤销的订单。
     * <p>
     * {
     * "account-id": "100009",
     * "amount": "10.1",
     * "price": "100.1",
     * "source": "api",
     * "symbol": "ethusdt",
     * "type": "buy-limit"
     * }
     *
     * @param accountId
     * @param symbol
     * @param side
     * @param size
     * @return
     */
    public ApiResponse<List<OrdersDetail>> openOrders(String accountId, String symbol, String side, String size) {
        HashMap<String, String> map = new HashMap<>();
        map.put("account-id", accountId);
        map.put("symbol", symbol);
        if (side != null) {
            map.put("side", side);
        }
        map.put("size", size);
        ApiResponse<List<OrdersDetail>> response = get("/v1/order/openOrders", map, new TypeReference<ApiResponse<List<OrdersDetail>>>() {
        });
        return response;
    }

    public ApiResponse<List<OrdersDetail>> openAllOrders(String accountId, String symbol, String size) {
        return this.openOrders(accountId, symbol, null, size);
    }

    public ApiResponse<List<OrdersDetail>> openBuyOrders(String accountId, String symbol, String size) {
        return this.openOrders(accountId, symbol, "buy", size);
    }

    public ApiResponse<List<OrdersDetail>> openSellOrders(String accountId, String symbol, String size) {
        return this.openOrders(accountId, symbol, "sell", size);
    }
    // ----------------------------------------行情API-------------------------------------------

    /**
     * GET /market/history/kline 获取K线数据
     *
     * @param symbol
     * @param period
     * @param size
     * @return
     */
    public KlineResponse kline(String symbol, String period, String size) {
        HashMap map = new HashMap();
        map.put("symbol", symbol);
        map.put("period", period);
        map.put("size", size);
        KlineResponse resp = get("/market/history/kline", map, new TypeReference<KlineResponse<List<Kline>>>() {
        });
        return resp;
    }

    /**
     * GET /market/detail/merged 获取聚合行情(Ticker)
     *
     * @param symbol
     * @return
     */
    public MergedResponse merged(String symbol) {
        HashMap map = new HashMap();
        map.put("symbol", symbol);
        MergedResponse resp = get("/market/detail/merged", map, new TypeReference<MergedResponse<List<Merged>>>() {
        });
        return resp;
    }

    /**
     * GET /market/depth 获取 MarketConfig Depth 数据
     *
     * @param request
     * @return
     */
    public DepthResponse depth(DepthRequest request) {
        HashMap map = new HashMap();
        map.put("symbol", request.getSymbol());
        map.put("type", request.getType());

        DepthResponse resp = get("/market/depth", map, new TypeReference<DepthResponse<List<Depth>>>() {
        });
        return resp;
    }

    /**
     * GET /market/trade 获取 Trade Detail 数据
     *
     * @param symbol
     * @return
     */
    public TradeResponse trade(String symbol) {
        HashMap map = new HashMap();
        map.put("symbol", symbol);
        TradeResponse resp = get("/market/trade", map, new TypeReference<TradeResponse>() {
        });
        return resp;
    }

    /**
     * GET /market/history/trade 批量获取最近的交易记录
     *
     * @param symbol
     * @param size
     * @return
     */
    public HistoryTradeResponse historyTrade(String symbol, String size) {
        HashMap map = new HashMap();
        map.put("symbol", symbol);
        map.put("size", size);
        HistoryTradeResponse resp = get("/market/history/trade", map, new TypeReference<HistoryTradeResponse<List<Trade<List<TradeBean>>>>>() {
        });
        return resp;
    }

    /**
     * GET /market/detail 获取 MarketConfig Detail 24小时成交量数据
     *
     * @param symbol
     * @return
     */
    public DetailResponse detail(String symbol) {
        HashMap<String, String> map = new HashMap<>();
        map.put("symbol", symbol);
        DetailResponse resp = get("/market/detail", map, new TypeReference<DetailResponse<Details>>() {
        });
        return resp;
    }


    /**
     * GET /v1/common/symbols 查询系统支持的所有交易对及精度
     *
     * @param symbol
     * @return
     */
    public SymbolsResponse symbols(String symbol) {
        HashMap map = new HashMap();
        map.put("symbol", symbol);
        SymbolsResponse resp = get("/v1/common/symbols", map, new TypeReference<SymbolsResponse<Symbols>>() {
        });
        return resp;
    }

    /**
     * GET /v1/common/currencys 查询系统支持的所有币种
     *
     * @param symbol
     * @return
     */
    public CurrencysResponse currencys(String symbol) {
        HashMap map = new HashMap();
        map.put("symbol", symbol);
        CurrencysResponse resp = get("/v1/common/currencys", map, new TypeReference<CurrencysResponse>() {
        });
        return resp;
    }

    /**
     * GET /v1/common/timestamp 查询系统当前时间
     *
     * @return
     */
    public TimestampResponse timestamp() {
        TimestampResponse resp = get("/v1/common/timestamp", null, new TypeReference<TimestampResponse>() {
        });
        return resp;
    }

    /**
     * GET /v1/account/accounts 查询当前用户的所有账户(即account-id)
     *
     * @return
     */
    public AccountsResponse accounts() {
        AccountsResponse resp = get("/v1/account/accounts", null, new TypeReference<AccountsResponse<List<Accounts>>>() {
        });
        return resp;
    }

    /**
     * GET /v1/account/accounts/{account-id}/balance 查询指定账户的余额
     *
     * @param accountId
     * @return
     */
    public BalanceResponse balance(String accountId) {
        BalanceResponse resp = get("/v1/account/accounts/" + accountId + "/balance", null, new TypeReference<BalanceResponse<Balance<List<BalanceBean>>>>() {
        });
        return resp;
    }

    /**
     * POST /v1/order/orders/{order-id}/submitcancel 申请撤销一个订单请求
     *
     * @param orderId
     * @return
     */
    public SubmitcancelResponse submitcancel(String orderId) {
        SubmitcancelResponse resp = post("/v1/order/orders/" + orderId + "/submitcancel", null, new TypeReference<SubmitcancelResponse>() {
        });
        return resp;
    }

    /**
     * POST /v1/order/orders/batchcancel 批量撤销订单
     *
     * @param orderList
     * @return
     */
    public BatchcancelResponse submitcancels(List orderList) {
        BatchcancelResponse resp = post("/v1/order/orders/batchcancel", orderList, new TypeReference<BatchcancelResponse<Batchcancel<List, BatchcancelBean>>>() {
        });
        return resp;
    }

    /**
     * GET /v1/order/orders/{order-id} 查询某个订单详情
     *
     * @param orderId
     * @return
     */
    public OrdersDetailResponse ordersDetail(String orderId) {
        OrdersDetailResponse resp = get("/v1/order/orders/" + orderId, null, new TypeReference<OrdersDetailResponse<OrdersDetail>>() {
        });
        return resp;
    }


    /**
     * GET /v1/order/orders/{order-id}/matchresults 查询某个订单的成交明细
     *
     * @param orderId
     * @return
     */
    public MatchresultsOrdersDetailResponse matchresults(String orderId) {
        MatchresultsOrdersDetailResponse resp = get("/v1/order/orders/" + orderId + "/matchresults", null, new TypeReference<MatchresultsOrdersDetailResponse>() {
        });
        return resp;
    }

    public IntrustDetailResponse intrustOrdersDetail(IntrustOrdersDetailRequest req) {
        HashMap map = new HashMap();
        map.put("symbol", req.symbol);
        map.put("states", req.states);
//    map.put("symbol",symbol);
//    map.put("symbol",symbol);
//    map.put("symbol",symbol);
//    map.put("symbol",symbol);

        IntrustDetailResponse resp = get("/v1/order/orders/", map, new TypeReference<IntrustDetailResponse<List<IntrustDetail>>>() {
        });
        return resp;
    }

//  public IntrustDetailResponse getALlOrdersDetail(String orderId) {
//    IntrustDetailResponse resp = get("/v1/order/orders/"+orderId, null,new TypeReference<IntrustDetailResponse>() {});
//    return resp;
//  }


    // send a GET request.
    <T> T get(String uri, Map<String, String> params, TypeReference<T> ref) {
        if (params == null) {
            params = new HashMap<>();
        }
        return call("GET", uri, null, params, ref);
    }

    // send a POST request.
    <T> T post(String uri, Object object, TypeReference<T> ref) {
        return call("POST", uri, object, new HashMap<String, String>(), ref);
    }

    // call api by endpoint.
    <T> T call(String method, String uri, Object object, Map<String, String> params,
               TypeReference<T> ref) {
        Request.Builder builder;
        Request request;
        if (this.accessKeyId != null && this.accessKeySecret != null) {
            ApiSignature sign = new ApiSignature();
            sign.createSignature(this.accessKeyId, this.accessKeySecret, method, API_HOST, uri, params);
        }
        try {
            if ("POST".equals(method)) {
                RequestBody body = RequestBody.create(JSON, JsonUtil.writeValue(object));
                builder = new Request.Builder().url(API_URL + uri + "?" + toQueryString(params)).post(body);
            } else {
                builder = new Request.Builder().url(API_URL + uri + "?" + toQueryString(params)).get();
            }
            if (this.assetPassword != null) {
                builder.addHeader("AuthData", authData());
            }
            request = builder.build();
            Response response = client.newCall(request).execute();
            String s = response.body().string();
            return JsonUtil.readValue(s, ref);
        } catch (IOException e) {
            throw new ApiException(e);
        }
    }

    String authData() {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        md.update(this.assetPassword.getBytes(StandardCharsets.UTF_8));
        md.update("hello, moto".getBytes(StandardCharsets.UTF_8));
        Map<String, String> map = new HashMap<>();
        map.put("assetPwd", DatatypeConverter.printHexBinary(md.digest()).toLowerCase());
        try {
            return ApiSignature.urlEncode(JsonUtil.writeValue(map));
        } catch (IOException e) {
            throw new RuntimeException("Get json failed: " + e.getMessage());
        }
    }

    // Encode as "a=1&b=%20&c=&d=AAA"
    String toQueryString(Map<String, String> params) {
        return String.join("&", params.entrySet().stream().map((entry) -> {
            return entry.getKey() + "=" + ApiSignature.urlEncode(entry.getValue());
        }).collect(Collectors.toList()));
    }

    // create OkHttpClient:with proxy
    static OkHttpClient createOkHttpClient(String ip, int port) {
        return new Builder().connectTimeout(CONN_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS).writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, port)))
                .build();
    }

    // create OkHttpClient: without proxy
    static OkHttpClient createOkHttpClient() {
        return new Builder().connectTimeout(CONN_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS).writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .build();
    }
}


/**
 * API签名，签名规范：
 * <p>
 * http://docs.aws.amazon.com/zh_cn/general/latest/gr/signature-version-2.html
 *
 * @Date 2018/1/14
 * @Time 16:02
 */
class ApiSignature {

    final Logger log = LoggerFactory.getLogger(getClass());

    static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss");
    static final ZoneId ZONE_GMT = ZoneId.of("Z");

    /**
     * 创建一个有效的签名。该方法为客户端调用，将在传入的params中添加AccessKeyId、Timestamp、SignatureVersion、SignatureMethod、Signature参数。
     *
     * @param appKey       AppKeyId.
     * @param appSecretKey AppKeySecret.
     * @param method       请求方法，"GET"或"POST"
     * @param host         请求域名，例如"be.huobi.com"
     * @param uri          请求路径，注意不含?以及后的参数，例如"/v1/api/info"
     * @param params       原始请求参数，以Key-Value存储，注意Value不要编码
     */
    public void createSignature(String appKey, String appSecretKey, String method, String host,
                                String uri, Map<String, String> params) {
        StringBuilder sb = new StringBuilder(1024);
        sb.append(method.toUpperCase()).append('\n') // GET
                .append(host.toLowerCase()).append('\n') // Host
                .append(uri).append('\n'); // /path
        params.remove("Signature");
        params.put("AccessKeyId", appKey);
        params.put("SignatureVersion", "2");
        params.put("SignatureMethod", "HmacSHA256");
        params.put("Timestamp", gmtNow());
        // build signature:
        SortedMap<String, String> map = new TreeMap<>(params);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key).append('=').append(urlEncode(value)).append('&');
        }
        // remove last '&':
        sb.deleteCharAt(sb.length() - 1);
        // sign:
        Mac hmacSha256 = null;
        try {
            hmacSha256 = Mac.getInstance("HmacSHA256");
            if (appSecretKey != null) {
                SecretKeySpec secKey =
                        new SecretKeySpec(appSecretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
                hmacSha256.init(secKey);
            }
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("No such algorithm: " + e.getMessage());
        } catch (InvalidKeyException e) {
            throw new RuntimeException("Invalid key: " + e.getMessage());
        }
        String payload = sb.toString();
        byte[] hash = hmacSha256.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        String actualSign = Base64.getEncoder().encodeToString(hash);
        params.put("Signature", actualSign);
        if (log.isDebugEnabled()) {
            log.debug("Dump parameters:");
            for (Map.Entry<String, String> entry : params.entrySet()) {
                log.debug("  key: " + entry.getKey() + ", value: " + entry.getValue());
            }
        }
    }

    /**
     * 使用标准URL Encode编码。注意和JDK默认的不同，空格被编码为%20而不是+。
     *
     * @param s String字符串
     * @return URL编码后的字符串
     */
    public static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("UTF-8 encoding not supported!");
        }
    }

    /**
     * Return epoch seconds
     */
    long epochNow() {
        return Instant.now().getEpochSecond();
    }

    String gmtNow() {
        return Instant.ofEpochSecond(epochNow()).atZone(ZONE_GMT).format(DT_FORMAT);
    }
}


class JsonUtil {

    public static String writeValue(Object obj) throws IOException {
        return objectMapper.writeValueAsString(obj);
    }

    public static <T> T readValue(String s, TypeReference<T> ref) throws IOException {
        return objectMapper.readValue(s, ref);
    }

    static final ObjectMapper objectMapper = createObjectMapper();

    static ObjectMapper createObjectMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.KEBAB_CASE);
        mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        // disabled features:
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

}
