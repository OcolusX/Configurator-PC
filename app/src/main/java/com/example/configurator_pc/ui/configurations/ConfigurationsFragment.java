package com.example.configurator_pc.ui.configurations;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.configurator_pc.R;
import com.example.configurator_pc.databinding.FragmentConfigurationsBinding;
import com.example.configurator_pc.model.Configuration;
import com.example.configurator_pc.model.User;

import java.util.LinkedList;
import java.util.List;

public class ConfigurationsFragment extends Fragment {

    public static final String TAG_POSITION = "TAG_POSITION";
    private FragmentConfigurationsBinding binding;
    private RecyclerView configurationsRecycler;
    private ConfigurationsViewModel configurationsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentConfigurationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Инициализируем RecyclerView данного фрагмента
        configurationsRecycler = root.findViewById(R.id.configurations_recycler);
        configurationsRecycler.setLayoutManager(new LinearLayoutManager(root.getContext()));

        // Запрашиваем ViewModel данного фрагмента
        configurationsViewModel = new ViewModelProvider(
                requireActivity(),
                new ConfigurationsViewModelFactory(requireActivity().getApplication())
        ).get(ConfigurationsViewModel.class);

        // Запрашиваем пользователя у ViewModel и подписываемся на его получение, если пользователь
        // ещё не был загружен. В обоих вариантах подписывамся на получения списка сборок и
        // сохраянем пользователя в настройках.
        MutableLiveData<User> userLiveData = configurationsViewModel.getUser();
        if (userLiveData.getValue() == null) {
            userLiveData.observe(getViewLifecycleOwner(), user -> {
                observeDataChange(configurationsViewModel.getData());
                configurationsViewModel.saveUser();
            });
        } else {
            observeDataChange(configurationsViewModel.getData());
            configurationsViewModel.saveUser();
        }

        // Устанавливаем обработчик нажатия на кнопку добавления сборки
        root.findViewById(R.id.addConfigurationButton)
                .setOnClickListener(v -> onAddConfigurationButtonClick());

        return root;
    }

    private void onAddConfigurationButtonClick() {
        // При нажатии на кнопку запрашиваем у пользователя имя для новой сборки,
        // создаём новую сборку и добавляем её в адаптер
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        EditText editText = new EditText(getContext());
        editText.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        );
        editText.setText(getString(R.string.configuration_name));
        builder.setView(editText);
        builder.setTitle(R.string.enter_configuration_name);
        builder.setPositiveButton(R.string.create_configuration, ((dialog, which) -> {
            Configuration configuration = new Configuration(
                    -1,
                    editText.getText().toString(),
                    configurationsViewModel.getUser().getValue(),
                    new LinkedList<>()
            );

            ConfigurationsAdapter adapter =
                    (ConfigurationsAdapter) configurationsRecycler.getAdapter();
            // Не забываем в адаптер добавить fromStoreComponent,
            // который был передан в ViewModel из магазина.
            if (adapter == null) {
                List<Configuration> configurationList = new LinkedList<>();
                configurationList.add(configuration);
                adapter = new ConfigurationsAdapter(
                        configurationList,
                        this,
                        configurationsViewModel.getFromStoreComponent()
                );
                configurationsRecycler.setAdapter(adapter);
            } else {
                adapter.addConfiguration(configuration);
                adapter.setFromStoreComponent(configurationsViewModel.getFromStoreComponent());
            }
            // Сохраняем новый список в ViewModel
            configurationsViewModel.getData().postValue(adapter.getConfigurationList());
        }));
        builder.setCancelable(true);
        builder.setNegativeButton(R.string.cancel, ((dialog, which) -> dialog.cancel()));
        builder.create().show();
    }

    // Подписывается на получение списка сборок
    private void observeDataChange(MutableLiveData<List<Configuration>> data) {
        data.observe(getViewLifecycleOwner(), configurations -> {
            // Добавляем полученный список в адаптер и не забываем про fromStoreComponent
            if (configurations != null) {
                ConfigurationsAdapter adapter =
                        (ConfigurationsAdapter) configurationsRecycler.getAdapter();
                if (adapter == null) {
                    configurationsRecycler.setAdapter(new ConfigurationsAdapter(
                            configurations,
                            this,
                            configurationsViewModel.getFromStoreComponent())
                    );
                } else {
                    adapter.changeList(configurations);
                    adapter.setFromStoreComponent(configurationsViewModel.getFromStoreComponent());
                }
            }
        });
    }


    public void onDestroyView() {
        super.onDestroyView();
        // Удаляем fromStoreComponent из ViewModel
        this.configurationsViewModel.setFromStoreComponent(null);
        this.binding = null;
    }
}
