package tcss450.uw.edu.team8app.utils;

import tcss450.uw.edu.team8app.R;

/**
 * A utility class used to get the themes from the styles.xml file.
 *
 * @author Jim Phan akari0@uw.edu
 */
public enum Themes {

    Default(R.style.Team8DefaultTheme),
    Dark(R.style.DarkTheme),
    FruitSalad(R.style.FruitSaladTheme);

    public static final String TAG = "Theme";
    private int mId;

    Themes(int id) {
        mId = id;
    }

    public int getId() {
        return mId;
    }

    public static Themes getTheme(String theme) {
        if (theme.equalsIgnoreCase(Dark.toString())) {
            return Dark;
        } else if (theme.equalsIgnoreCase(FruitSalad.toString()) || theme.equalsIgnoreCase("Fruit Salad")) {
            return FruitSalad;
        } else {
            return Default;
        }
    }
}
