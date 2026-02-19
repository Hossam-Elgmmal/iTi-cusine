package com.iti.cuisine.discover;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.imageview.ShapeableImageView;
import com.iti.cuisine.R;
import com.iti.cuisine.data.database_models.MealEntity;
import com.iti.cuisine.utils.glide.GlideManager;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class TodayMealAdapter extends RecyclerView.Adapter<TodayMealAdapter.TodayMealViewHolder> {

    private List<MealEntity> todayMeals;

    private static Consumer<String> onItemClick;
    private static BiConsumer<String, String> onCountryClick;

    public TodayMealAdapter(List<MealEntity> todayMeals) {
        this.todayMeals = todayMeals;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setTodayMeals(List<MealEntity> todayMeals) {
        this.todayMeals = todayMeals;
        notifyDataSetChanged();
    }

    public void setOnItemClick(Consumer<String> onItemClick) {
        TodayMealAdapter.onItemClick = onItemClick;
    }

    public void setOnCountryClick(BiConsumer<String, String> onCountryClick) {
        TodayMealAdapter.onCountryClick = onCountryClick;
    }

    @NonNull
    @Override
    public TodayMealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meal, parent, false);

        return new TodayMealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodayMealViewHolder holder, int position) {
        holder.bind(todayMeals.get(position));
    }

    @Override
    public int getItemCount() {
        return todayMeals.size();
    }

    public static class TodayMealViewHolder extends RecyclerView.ViewHolder {

        private final ShapeableImageView imageView;
        private final TextView titleTextView;
        private final Chip countryChip;
        private final View itemView;
        private final View backgroundView;

        public TodayMealViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            imageView = itemView.findViewById(R.id.meal_image);
            backgroundView = itemView.findViewById(R.id.backgroundView);
            titleTextView = itemView.findViewById(R.id.meal_title);
            countryChip = itemView.findViewById(R.id.meal_country_chip);
        }

        public void bind(MealEntity mealEntity) {
            titleTextView.setText(mealEntity.getTitle());
            countryChip.setText(mealEntity.getCountry());

            itemView.setOnClickListener(v -> onItemClick.accept(mealEntity.getId()));
            countryChip.setOnClickListener(v -> onCountryClick.accept(mealEntity.getCountry(), mealEntity.getCountryFlagUrl()));

            GlideManager.loadIntoAndShowBackground(mealEntity.getThumbnail(), imageView, backgroundView);
            GlideManager.loadImageIntoChip(mealEntity.getCountryFlagUrl(), countryChip);
        }
    }

}
