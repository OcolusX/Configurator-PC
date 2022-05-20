package com.example.configurator_pc.ui.store;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.configurator_pc.MainActivity;
import com.example.configurator_pc.R;
import com.example.configurator_pc.databinding.FragmentStoreBinding;
import com.example.configurator_pc.model.Component;
import com.example.configurator_pc.model.ComponentType;
import com.google.android.material.tabs.TabLayout;

import java.util.List;
import java.util.Objects;

public class StoreFragment extends Fragment {

    public static final String TAG_CONFIGURATION_POSITION = "TAG_CONFIGURATION_POSITION";
    public static final String TAG_PAGE = "TAG_PAGE";
    public static final String TAG_POSITION = "TAG_POSITION";

    private FragmentStoreBinding binding;
    private RecyclerView storeRecycler;
    private StoreViewModel storeViewModel;

    private int configurationPosition; // Аргумент - позиция сборки в списке ConfigurationsViewModel
    private ComponentType page;        // Аргумент - запрашиваемая на открытие вкладка
    private TabLayout storeTabs;       // Вкладки магазина

    // Слшуатель пролистывания
    private final RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            MutableLiveData<List<Component>> newData;
            super.onScrollStateChanged(recyclerView, newState);
            // Отслеживаем момент пролистывания до конца списка и совершаем ряд проверок:
            // 1. Происхоит ли в данный момент загрузка данных из ViewModel (если происходит,
            //    то нет смысла заново запрашивать подзагрузку списка);
            // 2. Закончились ли компоненты в БД.
            if (!recyclerView.canScrollVertically(1)
                    && newState == RecyclerView.SCROLL_STATE_IDLE
                    && !storeViewModel.isLoading()
                    && !storeViewModel.isOutOfComponents()) {
                newData = storeViewModel.upLoadData();
                newData.observe(getViewLifecycleOwner(), components -> {
                    if (components == null || components.isEmpty()) {
                        storeViewModel.outOfComponents();
                    } else {
                        MutableLiveData<List<Component>> data =
                                storeViewModel.getData(storeViewModel.getCurrentPage());
                        List<Component> componentList = data.getValue();
                        if (componentList.addAll(components)) {
                            data.setValue(componentList);
                        }
                    }
                });
            }
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // В зависимости от аргументов определяем, откуда был вызван данный фрагмент:
        // Если configurationPosition == -1, то магазин был открыт с экрана редактирования сборки;
        // Иначе - обычным способом (через BottomNavMenu)
        Bundle arguments = getArguments();
        ComponentType componentType = null;
        if (arguments == null) {
            page = null;
            configurationPosition = -1;
        } else {
            int typeId = arguments.getInt(TAG_PAGE, -1);
            page = typeId == -1 ? null : ComponentType.getById(typeId);
            configurationPosition = arguments.getInt(TAG_CONFIGURATION_POSITION, -1);
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentStoreBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Инициализируем storeRecycler
        storeRecycler = root.findViewById(R.id.store_recycler);
        storeRecycler.setLayoutManager(new LinearLayoutManager(root.getContext()));

        // Запрашиваем ViewModel
        storeViewModel = new ViewModelProvider(
                requireActivity(),
                new StoreViewModelFactory(requireActivity().getApplication())
        ).get(StoreViewModel.class);

        // Если страница не была передана в качестве аргумента, то запршиваем из ViewModel
        if (page == null) {
            page = storeViewModel.getCurrentPage();
        }

        // Инициализируем вкладки магазина;
        // Если магазин был открыт с экрана редактирования сборки (configurationPosition == -1),
        // то вкладки отображать не нужно, в качестве title на ActionBar надо отобразить
        // название открытой вкладки, а также выделить на BottomNavMenu вкладку configurations.
        // Таким образом, мы имитируем ситуацию, будто пользователь остался на экране редактирования
        // сборки и лишь открыл (как бы в отдельном окошке) запрошенную по клику вкладку.
        // В то же время, при обычном переходе через BottomNavMenu открывается полноценный магазин;
        storeTabs = root.findViewById(R.id.store_tabs);
        ;
        if (configurationPosition != -1) {
            storeTabs.removeAllViews();
            MainActivity activity = (MainActivity) getActivity();
            ActionBar supportActionBar = Objects.requireNonNull(activity).getSupportActionBar();
            Objects.requireNonNull(supportActionBar).setDisplayHomeAsUpEnabled(true);
            supportActionBar.setTitle(getTitle(page));
            activity.setSelectedItemId(R.id.navigation_configurations);
        } else {
            setupStoreTabs();
        }

        // Подписываемся на загрузку компонентов из ViewModel
        observeDataChange(storeViewModel.getData(page));

        // Устанавливаем слушатель пролистывания для подзагрузки списка компонентов
        storeRecycler.addOnScrollListener(scrollListener);

        return root;
    }

    /* renamed from: com.example.configurator_pc.ui.store.StoreFragment$3 */
    static /* synthetic */ class C00023 {
        static final /* synthetic */ int[] $SwitchMap$com$example$configurator_pc$model$ComponentType;

        static {
            int[] iArr = new int[ComponentType.values().length];
            $SwitchMap$com$example$configurator_pc$model$ComponentType = iArr;
            try {
                iArr[ComponentType.MOTHERBOARD.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$example$configurator_pc$model$ComponentType[ComponentType.CPU.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$example$configurator_pc$model$ComponentType[ComponentType.COOLER.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$example$configurator_pc$model$ComponentType[ComponentType.GRAPHICS_CARD.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$example$configurator_pc$model$ComponentType[ComponentType.RAM.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$example$configurator_pc$model$ComponentType[ComponentType.HDD.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$example$configurator_pc$model$ComponentType[ComponentType.SSD.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$example$configurator_pc$model$ComponentType[ComponentType.CASE.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$example$configurator_pc$model$ComponentType[ComponentType.POWER_SUPPLY.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
        }
    }

    private int getTitle(ComponentType page2) {
        switch (C00023.$SwitchMap$com$example$configurator_pc$model$ComponentType[page2.ordinal()]) {
            case 1:
                return R.string.tab_motherboard;
            case 2:
                return R.string.tab_CPU;
            case 3:
                return R.string.tab_cooler;
            case 4:
                return R.string.tab_graphics_card;
            case 5:
                return R.string.tab_RAM;
            case 6:
                return R.string.tab_HDD;
            case 7:
                return R.string.tab_SSD;
            case 8:
                return R.string.tab_case;
            case 9:
                return R.string.tab_power_supply;
            default:
                return -1;
        }
    }

    private void setupStoreTabs() {
        // Массив с иконками для вкладок
        int[] tabIcons = {
                R.drawable.ic_motherboard,
                R.drawable.ic_cpu,
                R.drawable.ic_cooler,
                R.drawable.ic_graphics_card,
                R.drawable.ic_ram,
                R.drawable.ic_hdd,
                R.drawable.ic_hdd,
                R.drawable.ic_case,
                R.drawable.ic_power_supply
        };

        for (int i = 0; i < tabIcons.length; i++) {
            // Связываем шаблон с каждой вкладкой, устанавливаем в него картинку и подпись
            View view = getLayoutInflater().inflate(R.layout.tab_item, null);
            TabLayout.Tab tab = storeTabs.getTabAt(i);
            if (tab != null) {
                ((ImageView) view.findViewById(R.id.tab_item_icon)).setImageResource(tabIcons[i]);
                ((TextView) view.findViewById(R.id.tab_item_text)).setText(tab.getText());
                tab.setCustomView(view);
                if (i == storeViewModel.getTabPosition()) {
                    storeTabs.selectTab(tab);
                }
            }
        }

        // Также реализуем переходы по вкладкам при помощи слушателя нажатия
        this.storeTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            // При нажатии на новую вкладку запрашиваем загрузку компонентов из ViewModel
            public void onTabSelected(TabLayout.Tab tab) {
                observeDataChange(storeViewModel.loadData(
                        ComponentType.getById(tab.getPosition() + 1))
                );
            }

            // При закрытии текущей вкладки удаляем список компонентов из адаптера
            public void onTabUnselected(TabLayout.Tab tab) {
                StoreAdapter adapter = (StoreAdapter) storeRecycler.getAdapter();
                if (adapter != null) {
                    adapter.removeList();
                }
            }

            public void onTabReselected(TabLayout.Tab tab) {

            }

        });
    }

    // Подписывается на получения данных
    public void observeDataChange(MutableLiveData<List<Component>> data) {
        data.observe(getViewLifecycleOwner(), components -> {
            if (components != null) {
                StoreAdapter adapter = (StoreAdapter) this.storeRecycler.getAdapter();
                if (adapter == null) {
                    this.storeRecycler.setAdapter(new StoreAdapter(
                            components,
                            this,
                            configurationPosition)
                    );
                } else {
                    adapter.changeList(components);
                }
            }
        });
    }

    // При закрытии магазина сохраняем открытую на данный момент вкладку
    public void onDestroyView() {
        super.onDestroyView();
        storeViewModel.saveTabPosition(storeViewModel.getCurrentPage().getId() - 1);
        this.binding = null;
    }
}
