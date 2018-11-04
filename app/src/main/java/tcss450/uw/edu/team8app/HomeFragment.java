package tcss450.uw.edu.team8app;


import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private TextView weatherLocation, weatherCurrentTemp, weatherCurrentMisc;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        if (getArguments() != null) {
            try {
                JSONArray weather = new JSONArray(getArguments().getString("weather"));
                JSONObject hourlyWeather = weather.getJSONObject(1);
                JSONObject dailyWeather = weather.getJSONObject(0);
                JSONArray hourlyWeatherData = hourlyWeather.getJSONArray("data");
                JSONArray dailyWeatherData = dailyWeather.getJSONArray("data");
                JSONObject currentWeather = hourlyWeatherData.getJSONObject(0);

                String packageName = getActivity().getPackageName();
                weatherLocation = v.findViewById(R.id.textView_weather_location);
                weatherCurrentTemp = v.findViewById(R.id.textView_weather_current_temp);
                weatherCurrentMisc = v.findViewById(R.id.textView_weather_current_misc);
                weatherLocation.setText(hourlyWeather.getString("city_name") + ", " + hourlyWeather.getString("state_code") + " " + weather.getJSONObject(2).getString("zip"));
                weatherCurrentTemp.setText(currentWeather.getString("temp") + " °C");
                weatherCurrentMisc.setText("Precipitation: " + currentWeather.getString("pop") + " %\nHumidity: " + currentWeather.getString("rh") + " %\nWind: " + currentWeather.getString("wind_spd") + " mph");
                ImageView currentWeatherIcon = v.findViewById(R.id.imageView_weather_current_icon);
                String currentWeatherIconID = hourlyWeatherData.getJSONObject(0).getJSONObject("weather").getString("icon");
                currentWeatherIcon.setImageResource(getResources().getIdentifier(currentWeatherIconID, "drawable", packageName));
                TextView current;
                String time = hourlyWeatherData.getJSONObject(0).getString("timestamp_local").split("T", 2)[1];
                time = time.split(":")[0];
                int timeAsInteger = Integer.parseInt(time);
                for (int i = 0; i < 24; i++) {
                    current = v.findViewById(getResources().getIdentifier("textView_weather_hourly_temp" + (i + 1), "id", packageName));
                    timeAsInteger = timeAsInteger % 25;
                    if (timeAsInteger == 0) {
                        timeAsInteger++;
                    }
                    current.setText(timeAsInteger + ":00\n" + hourlyWeatherData.getJSONObject(i).getString("temp") + " °C");
                    timeAsInteger++;
                }
                for (int i = 0; i < 10; i++) {
                    current = v.findViewById(getResources().getIdentifier("textView_weather_daily_day" + (i + 1), "id", packageName));
                    String date = dailyWeatherData.getJSONObject(i).getString("valid_date");
                    String[] dateParsed = date.split("-", 3);
                    date = dateParsed[1] + "/" + dateParsed[2];
                    current.setText(date);
                    current = v.findViewById(getResources().getIdentifier("textView_weather_daily_temp" + (i + 1), "id", packageName));
                    current.setText(dailyWeatherData.getJSONObject(i).getString("temp") + " °C");
                    ImageView icon = v.findViewById(getResources().getIdentifier("imageView_weather_daily_icon" + (i + 1), "id", packageName));
                    String iconID = dailyWeatherData.getJSONObject(i).getJSONObject("weather").getString("icon");
                    icon.setImageResource(getResources().getIdentifier(iconID, "drawable", packageName));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return v;
    }

}
