package com.recalot.unittests.helper;

import java.io.*;
import java.net.*;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Matthaeus.Gdaniec
 * Date: 02.08.13
 * Time: 16:05
 * To change this template use File | Settings | File Templates.
 */
public class WebRequest {

    public enum HTTPMethod {
        GET,
        POST,
        PUT,
        DELETE,
    }


    public static boolean Debug = true;

    public static WebResponse execute(String targetURL) {
        return execute(HTTPMethod.GET, targetURL);
    }

    public static WebResponse execute(HTTPMethod method, String targetURL) {
        return execute(method, targetURL, "");
    }

    public static WebResponse execute(HTTPMethod method, String targetURL, Map<String, String> parameter) throws UnsupportedEncodingException {
        StringBuilder builder = new StringBuilder();
        for(String key: parameter.keySet()){
            builder.append(URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(parameter.get(key), "UTF-8") +"&");
        }

        return execute(method, targetURL, builder.toString(), null);
    }

    public static WebResponse execute(HTTPMethod method, String targetURL, String parameter) {
        return execute(method, targetURL, parameter, null);
    }

    public static WebResponse execute(HTTPMethod method, String targetURL, String parameter, String proxyIp, int proxyPort) {
        return execute(method, targetURL, parameter, new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyIp, proxyPort)));
    }

    public static WebResponse execute(HTTPMethod method, String targetURL, String parameter, Proxy proxy) {
        if(Debug) System.out.println(String.format("Execute web response with targetURL: '%s' method: '%s' and parameter:'%s'", targetURL, method, parameter));
        URL url;
        HttpURLConnection connection = null;
        try {
            //Create connection
            url = new URL(targetURL);
            connection = proxy != null ? (HttpURLConnection) url.openConnection(proxy) : (HttpURLConnection) url.openConnection();

            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            connection.setUseCaches(false);




            switch (method) {
                case GET:
                    connection.setRequestMethod("GET");
                    break;
                case POST: {
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    connection.setRequestProperty("Content-Length", String.valueOf(parameter.length()));
                    connection.setRequestMethod("POST");
                    connection.setDoInput(true);
                    connection.setDoOutput(true);

                    //Send request
                    DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                    wr.writeBytes(parameter);
                    wr.flush();
                    wr.close();

                }
                break;
                case DELETE: {
                    connection.setRequestMethod("DELETE");
                }
                break;
                case PUT: {
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    connection.setRequestProperty("Content-Length", String.valueOf(parameter.length()));
                    connection.setRequestMethod("PUT");
                    connection.setDoOutput(true);
                    connection.setDoInput(true);

                    //Send request
                    DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                    wr.writeBytes(parameter);
                    wr.flush();
                    wr.close();
                }
                break;
            }
            long start = System.currentTimeMillis();
            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();

            if(Debug) System.out.println("Response time:" + (System.currentTimeMillis() - start) + " ms");
            if(Debug)  System.out.println(String.format("Response: '%s' \n %s", targetURL, response.toString()));

            return new WebResponse(connection.getResponseCode(), response.toString(), connection.getContentType());

        } catch (Exception e) {

            e.printStackTrace();
            return null;

        } finally {

            if (connection != null) {
                connection.disconnect();
            }
        }
    }


}
