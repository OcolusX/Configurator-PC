package com.example.configurator_pc.ui.configurations;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.configurator_pc.model.Component;
import com.example.configurator_pc.model.Configuration;
import com.example.configurator_pc.model.User;
import com.example.configurator_pc.repository.Repository;

import java.util.List;

/* renamed from: com.example.configurator_pc.ui.configurations.ConfigurationsViewModel */
public class ConfigurationsViewModel extends AndroidViewModel {

    private static final String PREFS_FILE = "Account";     // Название файла настроек пользователя
    private static final String DEFAULT_USER_NAME = "User"; // Имя пользователя по умлочанию
    private static final String PREF_USER_ID = "UserId";    // Ключ для id пользователя
    private static final String PREF_USER_NAME = "UserName";// Ключ для имени пользователя

    private final Repository repository;                    // Репозиторий для связи с БД
    private MutableLiveData<List<Configuration>> data;      // Список сборок
    private final MutableLiveData<User> user;               // Пользователь

    private Component fromStoreComponent = null;            // Компонент, переданный из магазина

    public ConfigurationsViewModel(@NonNull Application application) {
        super(application);
        repository = Repository.getInstance(application);
        SharedPreferences settings =
                application.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        // Синхронизируем пользователя с БД
        user = repository.saveUser(new User(
                settings.getInt(PREF_USER_ID, -1),
                settings.getString(PREF_USER_NAME, DEFAULT_USER_NAME))
        );
    }

    public MutableLiveData<User> getUser() {
        return user;
    }

    // Сохраняет пользователя в файлах настроек
    public void saveUser() {
        User user = this.user.getValue();
        if (user != null) {
            SharedPreferences.Editor editor =
                    getApplication().getSharedPreferences(PREFS_FILE, 0).edit();
            editor.putInt(PREF_USER_ID, user.getId());
            editor.putString(PREF_USER_NAME, user.getName());
            editor.apply();
        }
    }

    public MutableLiveData<List<Configuration>> getData() {
        if (data == null) {
            User user = this.user.getValue();
            if (user != null) {
                data = repository.loadConfigurationList(user);
            } else {
                data = new MutableLiveData<>();
            }
        }
        return data;
    }

    // Сохраняет в БД сборку с переданной позицией в списке
    public void saveConfiguration(int position) {
        List<Configuration> configurationList = data.getValue();
        if (configurationList != null && configurationList.size() > position) {
            repository.saveConfiguration(configurationList.get(position));
        }
    }

    // Удаляет из БД сборку с переданной позицей в списке
    public void deleteConfiguration(int position) {
        List<Configuration> configurationList = data.getValue();
        if (configurationList != null && configurationList.size() > position) {
            repository.deleteConfiguration(configurationList.get(position));
            configurationList.remove(position);
        }
    }

    // Переименовывает сборку с переданной позицией в списке
    public void renameConfiguration(int position, String name) {
        List<Configuration> configurationList = data.getValue();
        // Поскольку модель подразумевает использованени только final полей,
        // то мы не можем использовать сеттер для имени сборки,
        // поэтому находим нужную сборку в списке, удаляем, а на её место добавляем новую.
        if (configurationList != null && configurationList.size() > position) {
            Configuration configuration = configurationList.get(position);
            configurationList.remove(position);
            configurationList.add(position, new Configuration(
                    configuration.getId(),
                    name,
                    configuration.getCreator(),
                    configuration.getComponentList())
            );
        }
    }

    public void setFromStoreComponent(Component fromStoreComponent) {
        this.fromStoreComponent = fromStoreComponent;
    }

    public Component getFromStoreComponent() {
        return fromStoreComponent;
    }
}
