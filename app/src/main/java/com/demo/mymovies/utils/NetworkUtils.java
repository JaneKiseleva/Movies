package com.demo.mymovies.utils;


import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

//Тут осуществляем всю работу связанную с сетью
//Сохраняем все параметры нужные нам и их значения с помощью JSON
//Создаем метод buildULR, который будет формировать запрос (мы можем сортировать по популярности и по рейтингу, также у нас будут меняться нормера страниц) и возвращать URL
//Создаем метод JSONLoadTask, которые будут загружать данные из интернета, открываем связь.
//Cоздаем метод getJSONFromNetwork, который будет получать JSON из сети
public class NetworkUtils {

    private static final String BASE_URL = "http://api.themoviedb.org/3/discover/movie";
    private static final String BASE_URL_VIDEOS = "http://api.themoviedb.org/3/movie/%s/videos";
    private static final String BASE_URL_REVIEWS = "http://api.themoviedb.org/3/movie/%s/reviews";

    //сохраняем параметры
    private static final String PARAMS_API_KEY = "api_key";
    private static final String PARAMS_LANGUAGE = "language";
    private static final String PARAMS_SORT_BY = "sort_by";
    private static final String PARAMS_PAGE = "page";
    private static final String PARAMS_MIN_VOTE_COUNT = "vote_count.gte";

    //сохраняем значения
    private static final String API_KEY = "1503e86b43213697cfcf6806efd4e604";
    private static final String LANGUAGE = "ru-RU";
    private static final String SORT_BY_POPULARITY = "popularity.desc";
    private static final String SORT_BY_TOP_RATED = "vote_average.desc";
    private static final String MIN_VOTE_COUNT_VALUE = "1000";

    //создаем переменные типа int, к которым будет доступ из других классов (популярность, рейтинг)
    //чтобы мы могли создать метод сортировки, который принимает параметр типа int и в зависимости от переданного значения возвращает разный результат
    // так как у нас меняются сортировка и номера страниц, мы в наш метод buildULR заносим их в качестве параметров.
    public static final int POPULARITY = 0;
    public static final int TOP_RATED = 1;

    //строим ссылку на трейлер
    public static URL buildULRToVideos(int id) { //(int id, String lang)
        Uri uri = Uri.parse(String.format(BASE_URL_VIDEOS, id)).buildUpon()
                .appendQueryParameter(PARAMS_API_KEY, API_KEY)
                .appendQueryParameter(PARAMS_LANGUAGE, LANGUAGE)
                //.appendQueryParameter(PARAMS_LANGUAGE, lang)
                .build();
        try {
            return new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject getJSONForVideos(int id) { //(int id, String lang)
        JSONObject result = null;
        URL url = buildULRToVideos(id); //URL url = buildULRToVideos(id, lang);
        try {
            result = new JSONLoadTask().execute(url).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    //строим ссылку на список фильмов
    public static URL buildULR(int sortBY, int page) { //public static URL buildULR(int sortBY, int page, String lang) {
        URL result = null;
        //так как сортировка у нас может быть по двум разных критериям - популярности и рейтингу, то заводим еще переменную и создаем условие.
        String methodOfSort;
        if (sortBY == POPULARITY) {
            methodOfSort = SORT_BY_POPULARITY;
        } else {
            methodOfSort = SORT_BY_TOP_RATED;
        }
        //Получаем строку (ulr сайта), к которому можем теперь прикреплять запросы с помощью метода buildUpon().
        Uri uri = Uri.parse(BASE_URL).buildUpon()
                //прикрепляем параметры
                .appendQueryParameter(PARAMS_API_KEY, API_KEY)
                .appendQueryParameter(PARAMS_LANGUAGE, LANGUAGE)
                //.appendQueryParameter(PARAMS_LANGUAGE, lang)
                .appendQueryParameter(PARAMS_SORT_BY, methodOfSort)
                .appendQueryParameter(PARAMS_PAGE, Integer.toString(page))
                .appendQueryParameter(PARAMS_MIN_VOTE_COUNT, MIN_VOTE_COUNT_VALUE)
                .build();
        try {
            result = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static JSONObject getJSONFromNetwork(int sortBY, int page) { //public static JSONObject getJSONFromNetwork(int sortBY, int page, String lang)
        JSONObject result = null;
        URL url = buildULR(sortBY, page); //URL url = buildULR(sortBY, page, lang);
        try {
            result = new JSONLoadTask().execute(url).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    //создаем ссылку на отзывы
    public static URL buildULRToReviews(int id) { //public static URL buildULRToReviews(int id, String lang)
        Uri uri = Uri.parse(String.format(BASE_URL_REVIEWS, id)).buildUpon()
                //.appendQueryParameter(PARAMS_LANGUAGE, lang)
                .appendQueryParameter(PARAMS_API_KEY, API_KEY)
                .build();
        try {
            return new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject getJSONForReviews(int id) { //public static JSONObject getJSONForReviews(int id, String lang)
        JSONObject result = null;
        URL url = buildULRToReviews(id); //URL url = buildULRToReviews(id, lang);
        try {
            result = new JSONLoadTask().execute(url).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    //Новый способ загружать данные, у AsyncTask останавливается загрузка данных из инета при смене конфигурации, а у AsyncTaskLoader нет.
    public static class JSONLoader extends AsyncTaskLoader<JSONObject> {

        private Bundle bundle;
        private OnStartLoadingListener onStartLoadingListener;

        public interface OnStartLoadingListener {
            void onStartLoading();
        }

        public void setOnStartLoadingListener(OnStartLoadingListener onStartLoadingListener) {
            this.onStartLoadingListener = onStartLoadingListener;
        }

        public JSONLoader(@NonNull Context context, Bundle bundle) {
            super(context);
            this.bundle = bundle;
        }

        @Override
        protected void onStartLoading() {
            super.onStartLoading();
            if (onStartLoadingListener != null) {
                onStartLoadingListener.onStartLoading();
            }
            forceLoad();
        }

        @Nullable
        @Override
        public JSONObject loadInBackground() {
            if (bundle == null) {
                return null;
            }
            String urlAsString = bundle.getString("url");
            URL url = null;
            try {
                url = new URL(urlAsString);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            JSONObject result = null;
            if (url == null) {
                return null;
            }
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(reader);
                StringBuilder builder = new StringBuilder();
                String line = bufferedReader.readLine();
                while (line != null) {
                    builder.append(line);
                    line = bufferedReader.readLine();
                }
                result = new JSONObject(builder.toString());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return result;
        }
    }


    private static class JSONLoadTask extends AsyncTask<URL, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(URL... urls) {
            JSONObject result = null;
            if (urls == null && urls.length == 0) {
                return result;
            }
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) urls[0].openConnection();
                InputStream inputStream = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(reader);
                StringBuilder builder = new StringBuilder();
                String line = bufferedReader.readLine();
                while (line != null) {
                    builder.append(line);
                    line = bufferedReader.readLine();
                }
                result = new JSONObject(builder.toString());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return result;
        }
    }
}



//private static final String URL = "http://api.themoviedb.org/3/discover/movie?api_key=1503e86b43213697cfcf6806efd4e604&language=en-US&short_by
//http://api.themoviedb.org/3/movie/{movie_id}/videos/?api_key=1503e86b43213697cfcf6806efd4e604&language=en-US&short_by