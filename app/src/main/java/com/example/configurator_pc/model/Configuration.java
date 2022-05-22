package com.example.configurator_pc.model;

import java.util.LinkedList;
import java.util.List;

public class Configuration {
    private final List<Component> componentList;
    private final User creator;

    private final int id;
    private final String name;
    private final int[] types = new int[ComponentType.values().length];

    public Configuration(int id, String name, User creator, List<Component> componentList) {
        this.id = id;
        this.name = name;
        this.creator = creator;

        this.componentList = new LinkedList<>();
        for (Component component : componentList) {
            ComponentType type = component.getType();
            int i = type.getId() - 1;
            if (type == ComponentType.HDD) {
                if (this.types[i] < 2) {
                    this.componentList.add(component);
                    this.types[i]++;
                }
            } else if (this.types[i] < 1) {
                this.componentList.add(component);
                this.types[i]++;
            }
        }
    }

    public float getAveragePrice() {
        float sum = 0.0f;
        for (Component component : this.componentList) {
            sum += component.getAveragePrice();
        }
        return sum;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public User getCreator() {
        return this.creator;
    }

    public List<Component> getComponentList() {
        return this.componentList;
    }

    public void addComponent(Component component) {
        if (component != null) {
            ComponentType type = component.getType();
            int i = type.getId() - 1;
            if (type == ComponentType.HDD) {
                if (this.types[i] < 2) {
                    this.componentList.add(component);
                    this.types[i]++;
                } else {
                    boolean first = this.types[i] % 2 == 0;
                    for (int j = 0; j < this.componentList.size(); j++) {
                        if (this.componentList.get(j).getType() == ComponentType.HDD) {
                            if (first) {
                                this.componentList.remove(j);
                                this.componentList.add(component);
                                this.types[i]++;
                            } else {
                                first = true;
                            }
                        }
                    }
                }
            } else if (this.types[i] < 1) {
                this.componentList.add(component);
                this.types[i]++;
            } else {
                for (int j = 0; j < this.componentList.size(); j++) {
                    if (this.componentList.get(j).getType() == type) {
                        this.componentList.remove(j);
                        this.componentList.add(component);
                    }
                }
            }
        }
    }

    public void removeComponentById(int id) {
        for (int i = 0; i < componentList.size(); i++) {
            Component component = componentList.get(i);
            if (component.getId() == id) {
                componentList.remove(i);
                types[component.getType().getId() - 1]--;
                break;
            }
        }
    }
}
