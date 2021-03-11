package com.demo.mymovies.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.mymovies.R;
import com.demo.mymovies.data.Movie;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {
    private List<Movie> movies;
    //объекты интерфейсного типа и добавим на них сеттеры
    private onPosterClickListener onPosterClickListener;
    private onReachEndListener onReachEndListener;

    //конструктор
    public MovieAdapter () {
        movies = new ArrayList<>();
    }

    //создаем интерфейс (будем нажимать на постер и дальше будет происходить действие)
     public interface onPosterClickListener {
        void onPosterClick (int position);
    }

    //сеттер на объект интерфейса
    public void setOnPosterClickListener (MovieAdapter.onPosterClickListener onPosterClickListener) {
        this.onPosterClickListener = onPosterClickListener;
    }

    //создаем второй интерфейс и слушатель, чтобы подгружать данные о фильмах, когда он долистал список (это необходимо, чтобы не загружать все сразу, чтобы пользователь не тратил трафик и приложение быстро работало)
    //reach - достигать. Этот метод вызывается при достижение конца листа
    public interface onReachEndListener {
        void onReachEnd ();
    }

    //сеттер на объект интерфейса
    public void setOnReachEndListener(MovieAdapter.onReachEndListener onReachEndListener) {
        this.onReachEndListener = onReachEndListener;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.movie_item, viewGroup, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder movieViewHolder, int i) {
        if (movies.size() >= 20 && i > movies.size() - 4 && onReachEndListener !=null) {
            onReachEndListener.onReachEnd();
        }
        Movie movie = movies.get(i);
        //библиотека Picasso хороша тем, что упрощает работу с изображениями, и что она кешируется, и изображение будет загружаться, даже если нет интернета
        //Вызываем метод Picasso
        Picasso.get().load(movie.getPosterPath()).into(movieViewHolder.imageViewSmallPoster);
    }


    @Override
    public int getItemCount() {
        return movies.size();
    }

    class MovieViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageViewSmallPoster;

        //конструктор
        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewSmallPoster = itemView.findViewById(R.id.imageViewSmallPoster);
            //вызываем слушатель у нашего itemView и создаем анонимный класс слушателя
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onPosterClickListener !=null) {
                        onPosterClickListener.onPosterClick(getAdapterPosition());
                    }
                }
            });
        }
    }

    public void clear () {
        this.movies.clear();
        notifyDataSetChanged();
    }

    //сеттер (установили фильмы)
    public void setMovies(List<Movie> movies) {
        this.movies = movies;
        notifyDataSetChanged();
    }

    //Метод. (Добавили фильмы) Когда мы захотим листать список фильмов, мы захотим добавлять новые фильмы в этот же массив, но не заменять весь старый массив на новый
    public void addMovies (List<Movie> movies) {
        this.movies.addAll(movies);
        notifyDataSetChanged();
    }

    //геттер
    public List<Movie> getMovies() {
        return movies;
    }
}
