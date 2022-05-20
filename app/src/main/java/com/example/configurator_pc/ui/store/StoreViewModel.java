package com.example.configurator_pc.ui.store;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.example.configurator_pc.model.Component;
import com.example.configurator_pc.model.ComponentType;
import com.example.configurator_pc.repository.Repository;
import java.util.List;

public class StoreViewModel extends AndroidViewModel {

    private final Repository repository;            // Репозиторий для свзяи с БД
    private MutableLiveData<List<Component>> data;  // Список компонентов

    private ComponentType currentPage = ComponentType.MOTHERBOARD; // Текущая открытая вкладка
    private int tabPosition;                                       // Позиция открытой вклдаки

    private static final int LOAD_ITEMS_NUMBER = 30; // Количество загружаемых за раз компонентов
    private int lastIndex;                          // Индекс, с которого надо начинать
                                                    // загрузкукомпонентов из БД

    private boolean outOfComponents = false;        // Флаг состояния:
                                                    // true - если компоненты в БД кончились

    public StoreViewModel(Application application) {
        super(application);
        repository = Repository.getInstance(application);
    }

    public int getTabPosition() {
        return tabPosition;
    }

    public void saveTabPosition(int tabPosition) {
        this.tabPosition = tabPosition;
    }

    public ComponentType getCurrentPage() {
        return currentPage;
    }

    public boolean isOutOfComponents() {
        return outOfComponents;
    }

    public void outOfComponents() {
        outOfComponents = true;
    }

    // Возвращает true, если в данный момент происходит какой либо запрос к БД
    public boolean isLoading() {
        return repository.isConnecting();
    }

    public MutableLiveData<List<Component>> getData(ComponentType page) {
        if (data == null || page != currentPage) {
            currentPage = page;
            outOfComponents = false;
            data = repository.loadComponentList(page, lastIndex, lastIndex + LOAD_ITEMS_NUMBER - 1);
        }
        return this.data;
    }

    // Загружает список компонентов из БД
    public MutableLiveData<List<Component>> loadData(ComponentType page) {
        lastIndex = 0;
        currentPage = page;
        outOfComponents = false;
        data = repository.loadComponentList(page, lastIndex, lastIndex + LOAD_ITEMS_NUMBER - 1);
        return data;
    }

    // Подзагружает список из БД (отличается от loadData тем, что не сохраняет новый список в data,
    // нужно для загрузки новых компонентов при достижении конца прокрутки.
    public MutableLiveData<List<Component>> upLoadData() {
        lastIndex += LOAD_ITEMS_NUMBER;
        outOfComponents = false;
        return repository.loadComponentList(currentPage, lastIndex, lastIndex + LOAD_ITEMS_NUMBER - 1);
    }
}
