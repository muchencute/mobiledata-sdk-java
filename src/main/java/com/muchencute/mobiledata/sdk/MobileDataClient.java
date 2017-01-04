package com.muchencute.mobiledata.sdk;

import com.muchencute.commons.encrypt.AESGenerator;
import com.muchencute.commons.protocol.JERObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@SuppressWarnings("unused")
public final class MobileDataClient {

    private final static MobileDataClient instance = new MobileDataClient();
    private String mAccountName = "test";
    private String mSecret = "1234567890ABCDEF";
    private String mURL = "http://sandbox.muchencute.com/mobiledata/order.mc";

    private MobileDataClient() {
    }

    public static MobileDataClient getInstance() {
        return instance;
    }

    public void setAccountName(String accountName) {
        mAccountName = accountName;
    }

    public void setSecret(String secret) {
        mSecret = secret;
    }

    public void setURL(String URL) {
        mURL = URL;
    }

    public JSONObject order(String transId, String channelOrderId, String cellphone, int amount, String region) throws IOException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("transId", transId);
        jsonObject.put("channelOrderId", channelOrderId);
        jsonObject.put("cellphone", cellphone);
        jsonObject.put("amount", amount);
        jsonObject.put("region", region);
        jsonObject.put("accountName", mAccountName);

        String rawData = jsonObject.toString();
        String sign = AESGenerator.encrypt(rawData, mSecret);
        jsonObject.put("sign", sign);

        HttpPost httpPost = new HttpPost(mURL);
        httpPost.setEntity(new StringEntity(jsonObject.toString(), ContentType.create("application/json", StandardCharsets.UTF_8)));

        CloseableHttpClient httpClient = HttpClients.createDefault();

        ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

            @Override
            public String handleResponse(final HttpResponse response) throws IOException {
                int status = response.getStatusLine().getStatusCode();
                HttpEntity entity = response.getEntity();
                if (status >= 200 && status < 300) {
                    return entity != null ? EntityUtils.toString(entity) : null;
                } else {
                    JERObject jerObject = new JERObject();
                    jerObject.setError(status, entity.toString());
                    return jerObject.toString();
                }
            }

        };

        String response = httpClient.execute(httpPost, responseHandler);

        JSONObject responseJSON;
        try {
            responseJSON = new JSONObject();
        } catch (JSONException e) {
            e.printStackTrace();
            JERObject jerObject = new JERObject();
            jerObject.setError(99, e.getLocalizedMessage());
            return jerObject.getResult();
        }

        return responseJSON;
    }
}
