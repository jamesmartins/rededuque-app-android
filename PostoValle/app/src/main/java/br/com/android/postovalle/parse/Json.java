package br.com.android.postovalle.parse;

import org.json.JSONException;
import org.json.JSONObject;

import br.com.android.postovalle.model.UrlServer;
import br.com.android.postovalle.model.User;

public class Json {
    public static User toUser(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            User user = new User();
            user.id =  obj.getString("RD_userId");
            user.company = obj.getString("RD_userCompany");
            user.email = obj.getString("RD_userMail");
            user.name = obj.getString("RD_userName");
            user.type = obj.getString("RD_userType");
            user.jsonObject = obj;
            return user;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static JSONObject toJsonObject(String json) {
        try {
            return new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static UrlServer toUrlServer(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            UrlServer urlServer = new UrlServer();
            urlServer.urlDefault = obj.getString("URL1");
            urlServer.url2 = obj.getString("URL2");
            urlServer.url3 = obj.getString("URL3");
            urlServer.url4 = obj.getString("URL4");
            return urlServer;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
