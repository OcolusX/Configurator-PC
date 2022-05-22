package com.example.configurator_pc.ui.configurations.edit_configuration;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.configurator_pc.MainActivity;
import com.example.configurator_pc.R;
import com.example.configurator_pc.databinding.FragmentEditConfigurationBinding;
import com.example.configurator_pc.model.Component;
import com.example.configurator_pc.model.ComponentType;
import com.example.configurator_pc.model.Configuration;
import com.example.configurator_pc.model.Currency;
import com.example.configurator_pc.repository.Repository;
import com.example.configurator_pc.ui.configurations.ConfigurationsViewModel;
import com.example.configurator_pc.ui.configurations.ConfigurationsViewModelFactory;
import com.example.configurator_pc.ui.store.StoreFragment;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;

public class EditConfigurationFragment extends Fragment {

    public static final String TAG_COMPONENT_ID = "TAG_COMPONENT_ID";

    // Массив для id каждого layout
    private static final int[] edits = {
            R.id.edit_motherboard,
            R.id.edit_CPU,
            R.id.edit_cooler,
            R.id.edit_graphics_card,
            R.id.edit_RAM,
            R.id.edit_SSD,
            R.id.edit_case,
            R.id.edit_power_supply,
            R.id.edit_add_hdd1,
            R.id.edit_add_hdd2
    };

    // Тип компонента, за который отвечает каждый layout из edits
    private static final ComponentType[] types = {
            ComponentType.MOTHERBOARD,
            ComponentType.CPU,
            ComponentType.COOLER,
            ComponentType.GRAPHICS_CARD,
            ComponentType.RAM,
            ComponentType.SSD,
            ComponentType.CASE,
            ComponentType.POWER_SUPPLY,
            ComponentType.HDD,
            ComponentType.HDD
    };

    private Configuration configuration;
    private ConfigurationsViewModel configurationsViewModel;
    private FragmentEditConfigurationBinding binding;

    private int position;   // Аргумент - позиция сборки в списке ViewModel
    private boolean save;   // Флаг состояния, отвечающий за сохранение сборки
    // Если true - то при закртии фрагмента сборка сохранится в ViewModel

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentEditConfigurationBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // По умолчанию true, false - если осуществляем навигацию не по стрелке назад
        this.save = true;
        // Устанавливаем слушатели на каждый layout
        setupOnClickListeners(root);
        // Выделяем в BottomNavMenu вкладку configurations для корректного отображения переходов
        ((MainActivity) requireActivity()).setSelectedItemId(R.id.navigation_configurations);

        // Устанавливаем слушатели на кнопки переименования и удаления сборки
        root.findViewById(R.id.rename_configuration_button)
                .setOnClickListener(v -> onRenameConfigurationButton(root));
        root.findViewById(R.id.delete_configuration_button)
                .setOnClickListener(v -> onDeleteConfigurationButton());


        configurationsViewModel = new ViewModelProvider(
                requireActivity(),
                new ConfigurationsViewModelFactory(requireActivity().getApplication())
        ).get(ConfigurationsViewModel.class);
        // Получаем сборку по переданной позиции в списке в ViewModel в качестве аргумента
        Bundle arguments = getArguments();
        if (arguments != null) {
            position = arguments.getInt("TAG_POSITION");
        } else {
            position = configurationsViewModel.getLastPosition();
        }
        MutableLiveData<List<Configuration>> data = configurationsViewModel.getData();
        if (data != null) {
            // Получаем нужную сборку
            configuration = data.getValue().get(position);
            ((TextView) root.findViewById(R.id.text_configuration_name))
                    .setText(configuration.getName());
            // отображаем все компоненты сборки
            setupComponents(root);
        }
        // Отображаем среднюю цену сборки
        ((TextView) root.findViewById(R.id.edit_configuration_price)).

                setText(String.format(
                        Locale.getDefault(),
                        "%,.0f %c",
                        configuration.getAveragePrice(),
                        Currency.RUB.getSign()
                ));
        return root;
    }

    // Устанавливает все компоненты сборки в нужные layout
    private void setupComponents(View root) {
        for (Component component : configuration.getComponentList()) {
            // Определяем индекс layout для текущего компонента
            int i = 0;
            while (types[i] != component.getType()) {
                i++;
            }
            // Находим нужные layout на экране и настраиваем текст и картинку
            LinearLayout edit = root.findViewById(edits[i]);
            TextView text = (TextView) edit.getChildAt(1);
            text.setTextSize(9.0f);
            text.setText(component.getName());
            Picasso.get()
                    .load(Repository.HARDPRICE_URL + component.getImage())
                    .into((ImageView) edit.getChildAt(0));
            // При нажатии переходим на экран описания товара
            edit.setOnClickListener(v -> {
                // Не забываем снять флаг
                save = false;
                configurationsViewModel.savePosition(position);
                // В качестве аргументов передаём позицию текущей сборки в списке ViewModel,
                // а также id компонента, по которому кликнули
                Bundle bundle = new Bundle();
                bundle.putInt(StoreFragment.TAG_CONFIGURATION_POSITION, position);
                bundle.putInt(TAG_COMPONENT_ID, component.getId());
                NavHostFragment.findNavController(this).navigate(
                        R.id.action_editConfigurationFragment_to_storeItemDescriptionFragment,
                        bundle
                );
            });
        }
    }

    // Устанавливает слушатели нажатия на каждый layout
    private void setupOnClickListeners(View root) {
        for (int edit : edits) {
            root.findViewById(edit).setOnClickListener(v -> {
                for (int i = 0; i < edits.length; i++) {
                    // Находим нужный layout с массиве
                    if (edits[i] == edit) {
                        // Снимаем флаг состояния
                        save = false;
                        // Осуществляем переход в магазин
                        // В качестве аргумента передаём вкадку, которую хотим открыть
                        // и позицию текущей сборки в списке ViewModel
                        Bundle bundle = new Bundle();
                        bundle.putInt(StoreFragment.TAG_PAGE, types[i].getId());
                        bundle.putInt(StoreFragment.TAG_CONFIGURATION_POSITION, position);
                        NavHostFragment.findNavController(this).navigate(
                                R.id.action_global_navigation_store,
                                bundle
                        );
                    }
                }
            });
        }
    }

    public void onRenameConfigurationButton(View root) {
        // При помощи диалогового окна запрашиваем у пользователя новое имя для текущей сборки
        EditText editName = new EditText(getContext());
        editName.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        editName.setText(configuration.getName());
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(editName);
        builder.setPositiveButton(R.string.on_button_rename_text, (dialog, which) -> {
            // Проверяем, не совпадает ли новое имя с текущим и переименовываем текущую сборку
            String name = editName.getText().toString();
            if (!name.equals(configuration.getName())) {
                configurationsViewModel.renameConfiguration(position, name);
                configuration = configurationsViewModel.getData().getValue().get(position);
                ((TextView) root.findViewById(R.id.text_configuration_name))
                        .setText(configuration.getName());
            }
        });
        builder.setCancelable(true);
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        builder.create().show();
    }

    public void onDeleteConfigurationButton() {
        // При помощи диалогового окна запрашиваем подтверждение на удаление и удаляем сборку
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.confirmation_delete);
        builder.setPositiveButton(R.string.on_button_delete_text, ((dialog, which) -> {
            configurationsViewModel.deleteConfiguration(position);
            // Не забываем снять флаг состояния
            save = false;
            NavHostFragment.findNavController(this).navigateUp();
        }));
        builder.setNegativeButton(R.string.cancel, ((dialog, which) -> dialog.cancel()));
        builder.create().show();
    }

    public void onDestroyView() {
        super.onDestroyView();
        // Сохраняем сборку в ViewModel
        if (this.save) {
            this.configurationsViewModel.saveConfiguration(this.position);
        }
    }
}
