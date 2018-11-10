package tcss450.uw.edu.team8app.utils;

import tcss450.uw.edu.team8app.R;

/**
 * A utility class used to get the themes from the styles.xml file.
 * @author Jim Phan akari0@uw.edu
 */
public enum Themes {

    Default(R.style.AppTheme),
    Dark(R.style.DarkTheme),
    FruitSalad(R.style.FruitSaladTheme);

    public static final String TAG = "themes";
    private int mId;

    private Themes(int id) {
        mId = id;
    }

    public int getId() {
        return mId;
    }

    public static Themes getTheme(String theme) {
        if(theme.equals(Dark.toString())) {
            return Dark;
        } else if(theme.equals(FruitSalad)) {
            return FruitSalad;
        } else {
            return Default;
        }
    }
}
