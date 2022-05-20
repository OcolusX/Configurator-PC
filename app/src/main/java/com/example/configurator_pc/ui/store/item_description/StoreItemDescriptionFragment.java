package com.example.configurator_pc.ui.store.item_description;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.configurator_pc.MainActivity;
import com.example.configurator_pc.R;
import com.example.configurator_pc.model.Attribute;
import com.example.configurator_pc.model.Component;
import com.example.configurator_pc.model.Configuration;
import com.example.configurator_pc.model.Price;
import com.example.configurator_pc.ui.configurations.ConfigurationsViewModel;
import com.example.configurator_pc.ui.configurations.ConfigurationsViewModelFactory;
import com.example.configurator_pc.ui.configurations.edit_configuration.EditConfigurationFragment;
import com.example.configurator_pc.ui.store.StoreFragment;
import com.example.configurator_pc.ui.store.StoreViewModel;
import com.example.configurator_pc.ui.store.StoreViewModelFactory;
import com.example.configurator_pc.repository.Repository;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;

public class StoreItemDescriptionFragment extends Fragment {

    private Component component;

    private int componentId;            // Аргумент - id переданного компонента
    private int configurationPosition;  // Аргумент - позиция сборки из ConfigurationsViewModel

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_store_item_description, container, false);

        // Получаем компонент по переданным аргументами
        getComponent();

        // Заполняем страницу описания товара
        createView(inflater, container, root);

        // Инициализируем кнопки добавления в сборку и удаления из сборки;
        // Если текущий фрагмент был вызван из магазина (componentId == -1),
        // то отображаем кнопку добавления и прячем кнопку удаления. Иначе - наоборот;
        // Также если мы перешли из магазина, то выделяем в BottomNavMenu вкладку Store
        // Иначе - вкладку Configurations (имитируем для пользователя, что мы не в магазине)
        Button addButton = root.findViewById(R.id.add_to_configuration_button);
        Button removeButton = root.findViewById(R.id.remove_from_configuration_button);
        if (this.componentId == -1) {
            addButton.setVisibility(View.VISIBLE);
            removeButton.setVisibility(View.INVISIBLE);
            addButton.setOnClickListener(v -> onAddButtonClick());
            ((MainActivity) requireActivity()).setSelectedItemId(R.id.navigation_store);
        } else {
            addButton.setVisibility(View.INVISIBLE);
            removeButton.setVisibility(View.VISIBLE);
            removeButton.setOnClickListener(v -> onRemoveButtonClick());
            ((MainActivity) requireActivity()).setSelectedItemId(R.id.navigation_configurations);
        }

        return root;
    }

    private void onAddButtonClick() {
        // Запрашиваем ViewModel;
        // Если мы перешли из магазина, то добавляем текущий компонент в качестве fromStoreComponent
        // Иначе - ищем нужную сборку и добавляем;
        ConfigurationsViewModel configurationsViewModel = new ViewModelProvider(
                requireActivity(),
                new ConfigurationsViewModelFactory(requireActivity().getApplication())
        ).get(ConfigurationsViewModel.class);

        if (configurationPosition == -1) {
            configurationsViewModel.setFromStoreComponent(component);
        } else {
            List<Configuration> data = configurationsViewModel.getData().getValue();
            if (data != null) {
                data.get(configurationPosition).addComponent(component);
            }
        }
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigateUp();
        navController.navigateUp();
        ((MainActivity) getActivity()).setSelectedItemId(R.id.navigation_configurations);
    }

    private void onRemoveButtonClick() {
        // Запрашиваем ViewModel, ищем нужную сборку и удаляем из неё текущий компонент;
        ConfigurationsViewModel configurationsViewModel = (new ViewModelProvider(
                requireActivity(),
                new ConfigurationsViewModelFactory(requireActivity().getApplication())
        ).get(ConfigurationsViewModel.class));

        List<Configuration> data = configurationsViewModel.getData().getValue();
        if (data != null) {
            data.get(configurationPosition).removeComponentById(componentId);
        }
        NavHostFragment.findNavController(this).navigateUp();
        ((MainActivity) getActivity()).setSelectedItemId(R.id.navigation_configurations);
    }

    private void createView(LayoutInflater inflater, ViewGroup container, View root) {
        // Устанавливаем title для ActionBAr
        ((MainActivity) requireActivity()).getSupportActionBar().setTitle(R.string.description);

        // Устаналиваем имя компонента и загружаем его иконку
        ((TextView) root.findViewById(R.id.store_item_description_name)).setText(component.getName());
        ImageView icon = root.findViewById(R.id.store_item_description_image);
        icon.setMinimumHeight(icon.getWidth());
        Picasso.get().load(Repository.HARDPRICE_URL + component.getImage()).into(icon);
        LinearLayout attributesLayout = root.findViewById(R.id.store_item_description_attributes);

        // Отображаем список характеристик
        for (Attribute attribute : component.getAttributeList()) {
            View view = inflater.inflate(
                    R.layout.store_item_description_attribute,
                    container,
                    false
            );
            ((TextView) view.findViewById(R.id.attribute_name)).setText(attribute.getName());
            ((TextView) view.findViewById(R.id.attribute_value)).setText(attribute.getValue());
            attributesLayout.addView(view);
        }

        // Отображаем список цен
        LinearLayout pricesLayout = root.findViewById(R.id.store_item_description_prices_layout);
        for (Price price : component.getPriceList()) {
            View view = inflater.inflate(
                    R.layout.store_item_description_price,
                    container,
                    false
            );
            ((TextView) view.findViewById(R.id.price_store)).setText(price.getStoreName());
            ((TextView) view.findViewById(R.id.price_date))
                    .setText(MainActivity.dateFormat.format(price.getDate()));
            TextView priceText = view.findViewById(R.id.price_price);

            // При нажатии на цену открываем браузер и переходим в интернет-магазин
            priceText.setOnClickListener(v -> startActivity(
                    new Intent("android.intent.action.VIEW", Uri.parse(price.getUrl()))
            ));

            float priceValue = price.getPrice();
            if (priceValue == -1.0f) {
                priceText.setText("-");
            } else {
                priceText.setText(String.format(
                        Locale.getDefault(),
                        "%,.2f %s",
                        priceValue,
                        getString(R.string.price_rub)
                ));
            }
            pricesLayout.addView(view);
        }
    }

    private void getComponent() {
        // В зависимости от переданных аргументов определяем, откуда был вызван фрагмент:
        // Если componentId == -1, то мы перешли из магазина;
        // Если position == -1 (позиция компонента в списке StoreViewModel,
        // то с экрана редактирования сборки
        Bundle arguments = getArguments();
        if (arguments != null) {
            configurationPosition = arguments.getInt(StoreFragment.TAG_CONFIGURATION_POSITION);
            componentId = arguments.getInt(EditConfigurationFragment.TAG_COMPONENT_ID, -1);
            int position = arguments.getInt("TAG_POSITION", -1);
            if (componentId != -1) {
                // Запрашиваем ViewModel и ищем  в нужной сборке компонент по id
                ConfigurationsViewModel configurationsViewModel = (new ViewModelProvider(
                        requireActivity(),
                        new ConfigurationsViewModelFactory(requireActivity().getApplication())
                ).get(ConfigurationsViewModel.class));
                List<Configuration> data = configurationsViewModel.getData().getValue();
                if (data != null) {
                    for (Component component : data.get(configurationPosition).getComponentList()) {
                        if (component.getId() == this.componentId) {
                            this.component = component;
                            break;
                        }
                    }
                }
            } else if (position != -1) {
                // Запрашиваем ViewModel и получаем нужный компонент из списка
                StoreViewModel storeViewModel = new ViewModelProvider(
                        requireActivity(),
                        new StoreViewModelFactory(requireActivity().getApplication())
                ).get(StoreViewModel.class);

                MutableLiveData<List<Component>> data = storeViewModel.getData(storeViewModel.getCurrentPage());
                if (data != null) {
                    this.component = data.getValue().get(position);
                }
            }
        } else {
            this.component = null;
        }
    }
}
