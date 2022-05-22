package com.example.configurator_pc.ui.store;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.configurator_pc.R;
import com.example.configurator_pc.model.Component;
import com.example.configurator_pc.repository.Repository;
import com.squareup.picasso.Picasso;

import java.util.LinkedList;
import java.util.List;

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.StoreViewRow> {

    private List<Component> componentList;      // Список компонентов
    private final int configurationPosition;    // Позиция сборки из configurationsViewModel
    private final StoreFragment fragment;       // Фрагмент для получения R.string и навигации

    public StoreAdapter(List<Component> componentList,
                        StoreFragment fragment, int configurationPosition) {

        this.componentList = componentList;
        this.fragment = fragment;
        this.configurationPosition = configurationPosition;
    }

    @NonNull
    public StoreViewRow onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.store_item, parent, false);
        return new StoreViewRow(view);
    }

    public void onBindViewHolder(StoreViewRow holder, int position) {
        Component component = componentList.get(position);

        // Загружаем иконку компонента, устанавливаем имя и описание
        Picasso.get().load(Repository.HARDPRICE_URL + component.getImage()).into(holder.image);
        holder.name.setText(component.getName());
        holder.description.setText(component.getDescription());
        // При нажатии на элемент списка переходим на экран описания товара
        ((View) holder.image.getParent().getParent()).setOnClickListener(v -> {
            // В качестве аргументов передаём позицию комопнента в ViewModel и позицию сборки
            Bundle bundle = new Bundle();
            bundle.putInt("TAG_POSITION", position);
            bundle.putInt(StoreFragment.TAG_CONFIGURATION_POSITION, configurationPosition);
            NavHostFragment.findNavController(fragment).navigate(
                    R.id.action_navigation_store_to_storeItemDescriptionFragment,
                    bundle
            );
        });
    }

    public int getItemCount() {
        return componentList.size();
    }

    // Меняет текущий список на новый
    public void changeList(List<Component> componentList) {
        if (componentList != null) {
            this.componentList = componentList;
            notifyItemRangeChanged(0, componentList.size());
        }
    }

    // Удаляет текущий список
    public void removeList() {
        if (componentList != null && !componentList.isEmpty()) {
            notifyItemRangeRemoved(0, componentList.size());
            componentList.clear();
        }
    }

    static class StoreViewRow extends RecyclerView.ViewHolder {
        private final TextView description;
        private final ImageView image;
        private final TextView name;

        public StoreViewRow(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.store_item_image);
            name = itemView.findViewById(R.id.store_item_name);
            description = itemView.findViewById(R.id.description);
        }
    }
}
