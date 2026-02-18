package com.iti.cuisine.utils.glide;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.chip.Chip;
import com.iti.cuisine.R;

import java.util.LinkedList;
import java.util.Queue;

public class GlideManager {

    private static final DrawableCrossFadeFactory factory =
            new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();

    private static final Queue<Runnable> loadQueue = new LinkedList<>();
    private static boolean isLoading = false;

    private static void enqueue(Runnable task) {
        loadQueue.add(task);
        if (!isLoading) processNext();
    }

    private static void processNext() {
        Runnable next = loadQueue.poll();
        if (next == null) {
            isLoading = false;
            return;
        }
        isLoading = true;
        next.run();
    }

    private static void onLoadFinished() {
        processNext();
    }

    public static void loadInto(String url, ImageView imageView) {
        enqueue(() -> Glide.with(imageView)
                .load(url)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade(factory))
                .error(R.drawable.img_error_placeholder)
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource,
                                                @Nullable Transition<? super Drawable> transition) {
                        imageView.setImageDrawable(resource);
                        onLoadFinished();
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        onLoadFinished();
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        imageView.setImageDrawable(errorDrawable);
                        onLoadFinished();
                    }
                }));
    }

    public static void loadImageIntoChip(String imageUrl, Chip chip) {
        enqueue(() -> Glide.with(chip)
                .asBitmap()
                .load(imageUrl)
                .override(48, 48)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap bitmap,
                                                @Nullable Transition<? super Bitmap> transition) {
                        chip.setChipIcon(
                                new BitmapDrawable(chip.getContext().getResources(), bitmap)
                        );
                        onLoadFinished();
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        onLoadFinished();
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        chip.setChipIconResource(R.drawable.logo);

                        onLoadFinished();
                    }
                }));
    }
}