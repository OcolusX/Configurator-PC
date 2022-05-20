package com.example.configurator_pc.repository;

import android.app.Service;
import android.content.Context;
import android.net.ConnectivityManager;

import androidx.lifecycle.MutableLiveData;

import com.example.configurator_pc.MainActivity;
import com.example.configurator_pc.model.Attribute;
import com.example.configurator_pc.model.Component;
import com.example.configurator_pc.model.ComponentType;
import com.example.configurator_pc.model.Configuration;
import com.example.configurator_pc.model.Currency;
import com.example.configurator_pc.model.Price;
import com.example.configurator_pc.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Repository {
    public static final String HARDPRICE_URL = "https://hardprice.ru";
    private static Repository INSTANCE = null;
    private static final String THREAD_NAME = "ServerConnectionThread";
    private static final String url = "https://0a6a-185-81-66-97.eu.ngrok.io";
    private Thread connectionThread;
    private final HttpConnection httpConnection;

    private Repository(Context context) {
        this.httpConnection = new HttpConnection(context);
    }

    public static Repository getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new Repository(context);
        }
        return INSTANCE;
    }

    public boolean isConnecting() {
        return connectionThread != null && connectionThread.isAlive();
    }

    public MutableLiveData<User> saveUser(User user) {
        final MutableLiveData<User> liveData = new MutableLiveData<>();
        Runnable connectionTask = new Runnable() {
            public void run() {
                try {
                    JSONObject userJson = new JSONObject();
                    userJson.put("id", user.getId());
                    userJson.put("name", user.getName());
                    String id = null;
                    while (id == null) {
                        id = httpConnection.connect(
                                url + "/users",
                                HttpConnection.RequestMethod.POST,
                                userJson.toString()
                        );
                    }
                    liveData.postValue(new User(Integer.parseInt(id), user.getName()));
                } catch (InterruptedIOException exception) {
                    exception.printStackTrace();
                    liveData.postValue(null);
                } catch (IOException | RuntimeException | JSONException exception) {
                    exception.printStackTrace();
                    run();
                }
            }
        };
        if (isConnecting()) {
            this.connectionThread.interrupt();
        }
        Thread thread = new Thread(connectionTask, THREAD_NAME);
        this.connectionThread = thread;
        thread.start();
        return liveData;
    }

    public MutableLiveData<List<Configuration>> loadConfigurationList(User user) {
        final MutableLiveData<List<Configuration>> liveData = new MutableLiveData<>();
        Runnable connectionTask = new Runnable() {
            public void run() {
                try {
                    String jsonConfigurationString = null;
                    while (jsonConfigurationString == null) {
                        jsonConfigurationString = httpConnection.connect(
                                url + "/configurations/"+ user.getId(),
                                HttpConnection.RequestMethod.GET,
                                ""
                        );
                    }
                    JSONArray jsonConfigurationList = new JSONArray(jsonConfigurationString);
                    List<Configuration> configurationList = new LinkedList<>();
                    for (int i = 0; i < jsonConfigurationList.length(); i++) {
                        JSONObject jsonConfiguration = jsonConfigurationList.getJSONObject(i);
                        List<Component> componentList = new LinkedList<>();
                        JSONArray jsonComponentList = jsonConfiguration.getJSONArray("componentList");

                        for (int j = 0; j < jsonComponentList.length(); j++) {
                            JSONObject jsonComponent = jsonComponentList.getJSONObject(j);
                            JSONObject attributes = jsonComponent.getJSONObject("attributes");
                            List<Attribute> attributeList = new LinkedList<>();
                            Iterator<String> keys = attributes.keys();
                            while (keys.hasNext()) {
                                String next = keys.next();
                                attributeList.add(new Attribute(next, attributes.getString(next)));
                            }

                            JSONArray prices = jsonComponent.getJSONArray("prices");
                            List<Price> priceList = new LinkedList<>();
                            for (int k = 0; k < prices.length(); k++) {
                                JSONObject jsonPrice = prices.getJSONObject(k);
                                priceList.add(new Price(
                                        Float.parseFloat(jsonPrice.getString("price")),
                                        Currency.valueOf(jsonPrice.getString("currency")),
                                        jsonPrice.getString("store"),
                                        jsonPrice.getString("url"),
                                        MainActivity.dateFormat.parse(jsonPrice.getString("date"))
                                ));
                            }
                            componentList.add(new Component(
                                    jsonComponent.getInt("id"),
                                    jsonComponent.getString("name"),
                                    jsonComponent.getString("description"),
                                    ComponentType.getById(jsonComponent.getInt("type_id")),
                                    jsonComponent.getString("image"),
                                    attributeList,
                                    priceList
                            ));
                        }
                        configurationList.add(new Configuration(
                                jsonConfiguration.getInt("id"),
                                jsonConfiguration.getString("name"),
                                user,
                                componentList
                        ));
                    }
                    liveData.postValue(configurationList);
                } catch (InterruptedIOException exception) {
                    exception.printStackTrace();
                    liveData.postValue(null);
                } catch (IOException | RuntimeException | ParseException |
                        JSONException e) {
                    e.printStackTrace();
                    run();
                }
            }
        };
        if (isConnecting()) {
            connectionThread.interrupt();
        }
        connectionThread = new Thread(connectionTask, THREAD_NAME);
        connectionThread.start();
        return liveData;
    }

    public MutableLiveData<Integer> saveConfiguration(final Configuration configuration) {
        final MutableLiveData<Integer> liveData = new MutableLiveData<>();
        Runnable connectionTask = new Runnable() {
            public void run() {
                try {
                    JSONObject jsonConfiguration = new JSONObject();
                    jsonConfiguration.put("id", configuration.getId());
                    jsonConfiguration.put("creatorId", configuration.getCreator().getId());
                    jsonConfiguration.put("name", configuration.getName());
                    JSONArray componentIdList = new JSONArray();
                    for (Component component : configuration.getComponentList()) {
                        componentIdList.put(component.getId());
                    }
                    jsonConfiguration.put("componentIdList", componentIdList);
                    String requestBody = jsonConfiguration.toString();
                    String id = null;
                    while (id == null) {
                        id = httpConnection.connect(
                                url + "/configurations",
                                HttpConnection.RequestMethod.POST,
                                requestBody
                        );
                    }
                    liveData.postValue(Integer.parseInt(id));
                } catch (InterruptedIOException exception) {
                    exception.printStackTrace();
                    liveData.postValue(null);
                } catch (IOException | RuntimeException | JSONException e) {
                    e.printStackTrace();
                    run();
                }
            }
        };
        if (isConnecting()) {
            connectionThread.interrupt();
        }
        connectionThread = new Thread(connectionTask, THREAD_NAME);
        connectionThread.start();
        return liveData;
    }

    public void deleteConfiguration(final Configuration configuration) {
        Runnable connectionTask = new Runnable() {
            public void run() {
                try {
                    Repository.this.httpConnection.connect(
                            url  + "/configurations/" + configuration.getId(),
                            HttpConnection.RequestMethod.POST,
                            ""
                    );
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
        };
        if (isConnecting()) {
            connectionThread.interrupt();
        }
        connectionThread = new Thread(connectionTask, THREAD_NAME);
        connectionThread.start();
    }

    public MutableLiveData<List<Component>> loadComponentList(ComponentType type, int fromIndex,
                                                              int toIndex) {
        MutableLiveData<List<Component>> liveData = new MutableLiveData<>();
        Runnable connectionTask = new Runnable() {
            public void run() {
                try {
                    String jsonComponentString = null;
                    while (jsonComponentString == null) {
                        jsonComponentString = httpConnection.connect(
                                url + "/components/" + type.getId() + "?from_index=" + fromIndex + "&to_index=" + toIndex,
                                HttpConnection.RequestMethod.GET,
                                ""
                        );

                        if (jsonComponentString.equals("")) {
                            liveData.postValue(null);
                            return;
                        }
                    }
                    JSONArray jsonComponentList = new JSONArray(jsonComponentString);
                    List<Component> componentList = new LinkedList<>();
                    for (int i = 0; i < jsonComponentList.length(); i++) {
                        JSONObject jsonComponent = jsonComponentList.getJSONObject(i);
                        JSONObject attributes = jsonComponent.getJSONObject("attributes");
                        List<Attribute> attributeList = new LinkedList<>();
                        Iterator<String> keys = attributes.keys();
                        while (keys.hasNext()) {
                            String next = keys.next();
                            attributeList.add(new Attribute(next, attributes.getString(next)));
                        }
                        JSONArray prices = jsonComponent.getJSONArray("prices");
                        List<Price> priceList = new LinkedList<>();
                        for (int j = 0; j < prices.length(); j++) {
                            JSONObject jsonPrice = prices.getJSONObject(j);
                            priceList.add(new Price(
                                    Float.parseFloat(jsonPrice.getString("price")),
                                    Currency.valueOf(jsonPrice.getString("currency")),
                                    jsonPrice.getString("store"),
                                    jsonPrice.getString("url"),
                                    MainActivity.dateFormat.parse(jsonPrice.getString("date"))
                            ));
                        }
                        Component component = new Component(
                                jsonComponent.getInt("id"),
                                jsonComponent.getString("name"),
                                jsonComponent.getString("description"),
                                type, jsonComponent.getString("image"),
                                attributeList,
                                priceList
                        );
                        componentList.add(component);
                    }
                    liveData.postValue(componentList);
                } catch (InterruptedIOException exception) {
                    exception.printStackTrace();
                    liveData.postValue(null);
                } catch (JSONException | ParseException | RuntimeException | IOException e) {
                    e.printStackTrace();
                    run();
                }
            }
        };
        if (isConnecting()) {
            connectionThread.interrupt();
        }
        connectionThread = new Thread(connectionTask, THREAD_NAME);
        ;
        connectionThread.start();
        return liveData;
    }

    private static class HttpConnection {
        private final Context context;

        private enum RequestMethod {
            GET,
            POST
        }

        public HttpConnection(Context context) {
            this.context = context;
        }

        public String connect(String url, RequestMethod requestMethod, String requestBody) throws IOException {
            try {
                if (checkInternetConnection()) {
                    Scanner s = new Scanner(openHttpConnection(
                            url,
                            requestMethod,
                            requestBody)
                    ).useDelimiter("\\A");
                    if (!Thread.currentThread().isInterrupted()) {
                        return s.hasNext() ? s.next() : "";
                    }
                }
                throw new InterruptedIOException("Interrupted!");
            } catch (NullPointerException exception) {
                exception.printStackTrace();
                return null;
            }
        }

        private InputStream openHttpConnection(String urlString, RequestMethod requestMethod, String requestBody) throws IOException {
            URLConnection urlConnection = new URL(urlString).openConnection();
            if (urlConnection instanceof HttpURLConnection) {
                HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
                httpURLConnection.setAllowUserInteraction(false);
                httpURLConnection.setInstanceFollowRedirects(false);
                httpURLConnection.setRequestMethod(requestMethod.name());
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                httpURLConnection.setDoInput(true);
                if (requestMethod == RequestMethod.POST) {
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setRequestProperty("dataType", "json");
                }
                httpURLConnection.connect();
                if (requestMethod == RequestMethod.POST) {
                    httpURLConnection.getOutputStream().write(requestBody.getBytes());
                }
                int responseCode = httpURLConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                    return httpURLConnection.getInputStream();
                }
                return null;
            }
            throw new IOException("URL is not an http URL");
        }

        private boolean checkInternetConnection() {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) context.getSystemService(Service.CONNECTIVITY_SERVICE);

            if (connectivityManager.getNetworkInfo(0).getState() ==
                    android.net.NetworkInfo.State.CONNECTED ||
                    connectivityManager.getNetworkInfo(0).getState() ==
                            android.net.NetworkInfo.State.CONNECTING ||
                    connectivityManager.getNetworkInfo(1).getState() ==
                            android.net.NetworkInfo.State.CONNECTING ||
                    connectivityManager.getNetworkInfo(1).getState() ==
                            android.net.NetworkInfo.State.CONNECTED) {
                return true;
            } else if (
                    connectivityManager.getNetworkInfo(0).getState() ==
                            android.net.NetworkInfo.State.DISCONNECTED ||
                            connectivityManager.getNetworkInfo(1).getState() ==
                                    android.net.NetworkInfo.State.DISCONNECTED) {
                return false;
            }
            return false;
        }
    }
}
