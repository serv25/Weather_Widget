package xml.services;

import retrofit2.Call;
import retrofit2.http.GET;
import xml.models.Weather;


public interface OpenWeatherMapService {

    @GET("/data/2.5/weather?q=Kiev,ua&appid=99a2cb433897decf2990ed751ad43ed9")
    Call<Weather> getTemp();
}
