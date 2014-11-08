package de.uniHamburg.informatik.continuousvoice.settings;

import java.util.ArrayList;
import java.util.List;

public class GeneralSettings {
    
    //defaults
    private Language language = Language.EnUs;
    private List<SettingsChangedListener> listeners = new ArrayList<SettingsChangedListener>();
    private static GeneralSettings instance;
    
    public static GeneralSettings getInstance() {
        if (instance == null) {
            instance = new GeneralSettings();
        }
        return instance;
    }
    
    private GeneralSettings() {
        //empty private constuctor for singleton
    }
    
    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
        notifyListeners();
    }
    
    public void addSettingsChangedListener(SettingsChangedListener scl) {
        this.listeners.add(scl);
    }
    
    public void removeSettingsChangedListener(SettingsChangedListener scl) {
        this.listeners.remove(scl);
    }
    
    private void notifyListeners() {
        for (SettingsChangedListener scl: listeners) {
            scl.settingChanged();
        }
    }
    
}
