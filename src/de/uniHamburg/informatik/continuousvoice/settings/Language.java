package de.uniHamburg.informatik.continuousvoice.settings;

public enum Language {

    EnUs("en-US", "en", "eng-USA"), DeDe("de-DE", "de", "deu-DEU");

    private final String code2;
    private final String code4;
    private final String code6;

    private Language(String code4, String code2, String code6) {
        this.code2 = code2;
        this.code4 = code4;
        this.code6 = code6;
    }

    public boolean equalsName(String otherName) {
        boolean result = false;
        if (otherName == null) {
            return false;
        }
        
        switch (otherName.length()) {
            case 2:
                result = getCode2().equalsIgnoreCase(otherName);
                break;
            case 5:
                result = getCode4().equalsIgnoreCase(otherName);
                break;
            case 7:
                result = getCode6().equalsIgnoreCase(otherName);
                break;
        }
        return result;
    }

    public String getCode6() {
        return code6;
    }

    public String getCode4() {
        return code4;
    }

    public String getCode2() {
        return code2;
    }

    public static Language getByName(String name) {
        if (name.equalsIgnoreCase("de") || name.equalsIgnoreCase("de-de") || name.equalsIgnoreCase("deu-deu")) {
            return DeDe;
        } else if (name.equalsIgnoreCase("en") || name.equalsIgnoreCase("en-us") || name.equalsIgnoreCase("eng-usa")) {
            return EnUs;
        } else {
            return null;
        }
    }

    public String toString() {
        return getCode4();
    }
}
