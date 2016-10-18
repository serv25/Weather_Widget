package xml;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.serega.weatherwidget.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import xml.models.Weather;
import xml.services.OpenWeatherMapService;

public class MyWidgetProvider extends AppWidgetProvider {

    private static final String BASE_URL = "http://api.openweathermap.org";
    private static final double DIFFERENCE_CELSIUS_TO_KELVIN = 273.15;
    private static final String PLUS = "+";
    private static final String MINUS = "-";
    private static final String CELSIUS_MARK = "°C";
    private static String temperature = "Unknown";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        int widgetId;

        for (int i = 0; i < appWidgetIds.length; i++) {
            executeRequest();
            widgetId = appWidgetIds[i];


            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.my_widget_provider);
            remoteViews.setTextViewText(R.id.tv_temperature, temperature);

            Intent intent = new Intent(context, MyWidgetProvider.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            remoteViews.setOnClickPendingIntent(R.id.btn_refresh, pendingIntent);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }

    private void executeRequest() {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        OpenWeatherMapService service = retrofit.create(OpenWeatherMapService.class);

        Call<Weather> tokenResponseCall = service.getTemp();

        tokenResponseCall.enqueue(new Callback<Weather>() {
            @Override
            public void onResponse(Call<Weather> call, Response<Weather> response) {
                if (response.isSuccessful()) {

                    double kelvinTemperature = response.body().getMain().getTemp();
                    int celsiusTemp = (int) (kelvinTemperature - DIFFERENCE_CELSIUS_TO_KELVIN);

                    if (celsiusTemp == 0) {
                        temperature = String.valueOf(celsiusTemp) + CELSIUS_MARK;
                    } else if (kelvinTemperature < DIFFERENCE_CELSIUS_TO_KELVIN) {
                        temperature = MINUS + String.valueOf(celsiusTemp) + CELSIUS_MARK;

                    } else {
                        temperature = PLUS + String.valueOf(celsiusTemp) + CELSIUS_MARK;
                    }

                } else {
                    temperature = "Unknown";
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        Log.e("Error Body", jObjError.toString());
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<Weather> call, Throwable t) {
                Log.i("Failure", t.getMessage());
            }
        });
    }

}

