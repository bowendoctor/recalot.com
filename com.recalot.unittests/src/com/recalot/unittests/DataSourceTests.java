package com.recalot.unittests;

import com.recalot.common.communication.Message;
import com.recalot.common.interfaces.model.data.DataInformation;
import com.recalot.common.interfaces.model.data.DataSource;
import com.recalot.unittests.helper.WebRequest;
import com.recalot.unittests.helper.WebResponse;
import flexjson.JSONDeserializer;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by matthaeus.schmedding on 24.04.2015.
 */
public class DataSourceTests {
    public static String HOST = "http://localhost:8080/";
    public static String Path = "sources/";
    public static String JsonMimeType = "application/json; charset=UTF-8";


    public HashMap connectSource(String id, Map<String, String> params) throws UnsupportedEncodingException {
        WebResponse response = WebRequest.execute(WebRequest.HTTPMethod.PUT, HOST + Path, params);

        assertNotNull(response);
        assertEquals(response.getContentType(), JsonMimeType);
        assertNotNull(response.getBody());
        assertEquals(response.getResponseCode(), 200);


        HashMap map = new JSONDeserializer<HashMap>().deserialize(response.getBody());

        assertNotNull(map);
        assertEquals(map.get("id"), id);


        return map;
    }


    public HashMap accessSource(String id) {
        WebResponse response2 = WebRequest.execute(HOST + Path + id);

        assertNotNull(response2);
        assertEquals(response2.getContentType(), JsonMimeType);
        assertNotNull(response2.getBody());
        assertEquals(response2.getResponseCode(), 200);

        HashMap source = new JSONDeserializer<HashMap>().deserialize(response2.getBody());

        assertNotNull(source);
        assertEquals(source.get("id"), id);
        assertNotEquals(source.get("itemsCount"), 0);
        assertNotEquals(source.get("usersCount"), 0);
        assertNotEquals(source.get("interactionsCount"), 0);

        return source;
    }

    public HashMap deleteSource(String id) {

        WebResponse response2 = WebRequest.execute(WebRequest.HTTPMethod.DELETE, HOST + Path + id);

        assertNotNull(response2);
        assertEquals(response2.getContentType(), JsonMimeType);
        assertNotNull(response2.getBody());
        assertEquals(response2.getResponseCode(), 200);

        HashMap message2 = new JSONDeserializer<HashMap>().deserialize(response2.getBody());

        assertNotNull(message2);

        try {
            WebResponse response3 = WebRequest.execute(HOST + Path + id);
            assertNull(response3);

        } catch (Exception e) {

        }

        return message2;
    }

    @Test
    public void getDataSources() {
        WebResponse response = WebRequest.execute(HOST + Path);
        assertNotNull(response);
        assertEquals(response.getContentType(), JsonMimeType);
        assertNotNull(response.getBody());
        assertEquals(response.getResponseCode(), 200);

        List<HashMap> sources = new JSONDeserializer<List<HashMap>>().deserialize(response.getBody());

        //should be at least two: sql and movielens

        assertNotNull(sources);
        assertNotEquals(sources.size(), 0);
        assertNotEquals(sources.size(), 1);


        assertNotNull(sources.get(0));
        assertNotNull(sources.get(0).get("id"));

        String id = null;
        for (int i = 0; i < sources.size(); i++) {
            if (sources.get(i).get("state").equals("AVAILABLE")) {
                id = (String) sources.get(i).get("id");
                break;
            }
        }
        WebResponse response2 = WebRequest.execute(HOST + Path + id);
        assertNotNull(response2);
        assertEquals(response2.getContentType(), JsonMimeType);
        assertNotNull(response2.getBody());
        assertEquals(response2.getResponseCode(), 200);

        HashMap source = new JSONDeserializer<HashMap>().deserialize(response2.getBody());
        assertNotNull(source);
        assertNotNull(source.get("id"));
        assertNotNull(source.get("key"));
        assertNotNull(source.get("state"));
        assertNotNull(source.get("configuration"));

    }


    @Test
    public void connectSQLDataSource() throws UnsupportedEncodingException {
        String id = UUID.randomUUID().toString();

        Map<String, String> params = new Hashtable<>();
        params.put("data-builder-id", "mysql");
        params.put("source-id", id);
        params.put("sql-server", "mysql://localhost:3306");
        params.put("sql-database", "recalot_test");
        params.put("sql-username", "root");
        params.put("sql-password", "mysqlpassword");

        HashMap result = connectSource(id, params);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        HashMap source = accessSource(id);
        HashMap delete = deleteSource(id);
    }

    @Test
    public void connectRecalotWallpaperSQLDataSource() throws UnsupportedEncodingException {
        String host = "http://api.recalot.com/";
        String id = "wallpaper";
        try {

            WebResponse response = WebRequest.execute(host + Path + id);
            if (response == null) {
                initializeWallpaperSQLSource(host, id);

                Thread.sleep(2000);
            }

        } catch (Exception e) {
            initializeWallpaperSQLSource(host, id);
        }

    }

    private void initializeWallpaperSQLSource(String host, String id) throws UnsupportedEncodingException {

        Map<String, String> params = new Hashtable<>();
        params.put("data-builder-id", "mysql");
        params.put("source-id", id);
        params.put("sql-server", "mysql://localhost:3306");
        params.put("sql-database", "wallpaper");
        params.put("sql-username", "root");
        params.put("sql-password", "mQZv6rUN");


        WebResponse response = WebRequest.execute(WebRequest.HTTPMethod.PUT, host + Path, params);

        assertNotNull(response);
        assertEquals(response.getContentType(), JsonMimeType);
        assertNotNull(response.getBody());
        assertEquals(response.getResponseCode(), 200);


        HashMap map = new JSONDeserializer<HashMap>().deserialize(response.getBody());

        assertNotNull(map);
        assertEquals(map.get("id"), id);

    }

    @Test
    public void connectMovieLensDataSource() throws UnsupportedEncodingException {
        String id = UUID.randomUUID().toString();

        Map<String, String> params = new Hashtable<>();
        params.put("data-builder-id", "ml");
        params.put("source-id", id);
        params.put("dir", "C:/Privat/3_Uni/5_Workspaces/data/ml-1m");

        HashMap result = connectSource(id, params);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        HashMap source = accessSource(id);
        HashMap delete = deleteSource(id);
    }

    @Test
    public void deleteSQLDataSource() throws UnsupportedEncodingException {

        String id = UUID.randomUUID().toString();

        Map<String, String> params = new Hashtable<>();
        params.put("data-builder-id", "mysql");
        params.put("source-id", id);
        params.put("sql-server", "mysql://localhost:3306");
        params.put("sql-database", "recalot_test");
        params.put("sql-username", "root");
        params.put("sql-password", "mysqlpassword");

        WebResponse response = WebRequest.execute(WebRequest.HTTPMethod.PUT, HOST + Path, params);

        assertNotNull(response);
        assertEquals(response.getContentType(), JsonMimeType);
        assertNotNull(response.getBody());
        assertEquals(response.getResponseCode(), 200);

        HashMap map = new JSONDeserializer<HashMap>().deserialize(response.getBody());

        assertNotNull(map);
        assertEquals(map.get("id"), id);


        WebResponse response2 = WebRequest.execute(WebRequest.HTTPMethod.DELETE, HOST + Path + id);

        assertNotNull(response2);
        assertEquals(response2.getContentType(), JsonMimeType);
        assertNotNull(response2.getBody());
        assertEquals(response2.getResponseCode(), 200);

        HashMap message2 = new JSONDeserializer<HashMap>().deserialize(response2.getBody());

        assertNotNull(message2);

        try {
            WebResponse response3 = WebRequest.execute(HOST + Path + id);
            assertNull(response3);

        } catch (Exception e) {

        }
    }

    @Test
    public void deleteMovieLensDataSource() throws UnsupportedEncodingException {
        String id = UUID.randomUUID().toString();
        Map<String, String> params = new Hashtable<>();
        params.put("data-builder-id", "ml");
        params.put("source-id", id);
        params.put("dir", "C:/Privat/3_Uni/5_Workspaces/data/ml-1m");

        WebResponse response = WebRequest.execute(WebRequest.HTTPMethod.PUT, HOST + Path, params);

        assertNotNull(response);
        assertEquals(response.getContentType(), JsonMimeType);
        assertNotNull(response.getBody());
        assertEquals(response.getResponseCode(), 200);

        HashMap map = new JSONDeserializer<HashMap>().deserialize(response.getBody());

        assertNotNull(map);
        assertEquals(map.get("id"), id);

        WebResponse response2 = WebRequest.execute(WebRequest.HTTPMethod.DELETE, HOST + Path + id);

        assertNotNull(response2);
        assertEquals(response2.getContentType(), JsonMimeType);
        assertNotNull(response2.getBody());
        assertEquals(response2.getResponseCode(), 200);

        HashMap message2 = new JSONDeserializer<HashMap>().deserialize(response2.getBody());

        assertNotNull(message2);

        try {
            WebResponse response3 = WebRequest.execute(HOST + Path + id);
            assertNull(response3);

        } catch (Exception e) {

        }
    }
}
