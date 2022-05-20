package com.example.configurator_pc.ui.configurations;

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
import com.example.configurator_pc.model.ComponentType;
import com.example.configurator_pc.model.Configuration;
import com.example.configurator_pc.repository.Repository;
import com.squareup.picasso.Picasso;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class ConfigurationsAdapter extends RecyclerView.Adapter<ConfigurationsAdapter.ConfigurationViewRow> {

    private List<Configuration> configurationList;  // Выводимый список сборок
    private final ConfigurationsFragment fragment;  // Фрагмент для получения R.string и навигации
    private Component fromStoreComponent;           // Компонент, переданный из магазина

    public ConfigurationsAdapter(List<Configuration> configurationList,
            ConfigurationsFragment fragment, Component fromStoreComponent) {

        this.configurationList = configurationList == null ? new LinkedList<>() : configurationList;
        this.fragment = fragment;
        this.fromStoreComponent = fromStoreComponent;
    }

    // Добавляет в список сборку
    public void addConfiguration(Configuration configuration) {
        if (configurationList == null) {
            configurationList = new LinkedList<>();
        }
        configurationList.add(configuration);
        notifyItemInserted(configurationList.size() - 1);
    }

    // Меняет список на новый
    public void changeList(List<Configuration> configurationList) {
        if (configurationList != null) {
            this.configurationList = configurationList;
            notifyItemRangeChanged(0, configurationList.size());
        }
    }

    public List<Configuration> getConfigurationList() {
        return this.configurationList;
    }

    public void setFromStoreComponent(Component fromStoreComponent) {
        this.fromStoreComponent = fromStoreComponent;
    }

    @NonNull
    @Override
    public ConfigurationViewRow onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(com.example.configurator_pc.R.layout.configurations_item, parent, false);
        return new ConfigurationViewRow(view);
    }

    public void onBindViewHolder(@NonNull ConfigurationViewRow holder, int position) {
        Configuration configuration = configurationList.get(position);

        // Сначала загружаем икноку корпуса
        for (Component component : configuration.getComponentList()) {
            if (component.getType() == ComponentType.CASE) {
                Picasso.get()
                        .load(Repository.HARDPRICE_URL + component.getImage())
                        .into(holder.icon);
            }
        }

        // При нажатии на элемент списка переходим на экран редактирования сборки.
        // Если из магазина был передан fromStoreComponent,
        // то по клику автоматически добавляем его в сборку.
        ((View) ((View) holder.name.getParent()).getParent()).setOnClickListener(v -> {
            Component component = fromStoreComponent;
            if (component != null) {
                configuration.addComponent(component);
            }
            // В качестве аргументов передаём позицию текущей сборке в списке ViewModel
            Bundle bundle = new Bundle();
            bundle.putInt(ConfigurationsFragment.TAG_POSITION, position);
            NavHostFragment.findNavController(fragment).navigate(
                    com.example.configurator_pc.R.id.action_navigation_configurations_to_editConfigurationFragment,
                    bundle
            );
        });
        holder.name.setText(configuration.getName());
        // Выводим среднюю цену всборки рублях
        holder.price.setText(String.format(
                Locale.getDefault(),
                "%,.0f %s",
                configuration.getAveragePrice(),
                fragment.getString(com.example.configurator_pc.R.string.price_rub))
        );
    }

    public int getItemCount() {
        return configurationList.size();
    }

    static class ConfigurationViewRow extends RecyclerView.ViewHolder {
        private final ImageView icon;
        private final TextView name;
        private final TextView price;

        public ConfigurationViewRow(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.configurations_item_name);
            icon = itemView.findViewById(R.id.configurations_item_icon);
            price = itemView.findViewById(R.id.configurations_item_price);
        }
    }
}
