import org.json.JSONObject;
import org.json.JSONArray;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherService {

    private static final String API_KEY = "demo_yandex_weather_api_key_ca6d09349ba0";  // Мой ключ API
    private static final String API_URL = "https://api.weather.yandex.ru/v2/forecast";  // URL для запроса погоды

    public static void main(String[] args) {
        // Однажды побывал в Брюгге - сказочный город, решил указать его координаты (широта, долгота)
        double lat = 51.2093;
        double lon = 3.2247;

        // Получаем данные погоды
        String response = getWeatherData(lat, lon);

        // Если данные получены успешно
        if (response != null) {
            // Обрабатываем и выводим все данные
            JSONObject jsonResponse = new JSONObject(response);
            System.out.println("Response from Yandex Weather API:\n" + jsonResponse.toString(4));  // Выводим все данные в формате JSON

            // Извлекаем текущую температуру
            int temperature = jsonResponse.getJSONObject("fact").getInt("temp");
            System.out.println("\nТекущая температура: " + temperature + "°C");  // Выводим текущую температуру

            // Вычисляем среднюю температуру за последние 5 дней
            double averageTemperature = calculateAverageTemperature(jsonResponse, 5);  // Используем 5 дней как период
            System.out.println("\nСредняя температура за последние 5 дней: " + averageTemperature + "°C");
        }
    }

    /**
     * Метод для выполнения GET-запроса к Яндекс.Погоде и получения данных.
     *
     * @param lat - широта
     * @param lon - долгота
     * @return строка с ответом от сервиса (JSON)
     */
    public static String getWeatherData(double lat, double lon) {
        try {
            // Формируем URL с параметрами (широта и долгота)
            String urlString = API_URL + "?lat=" + lat + "&lon=" + lon;
            URL url = new URL(urlString);

            // Создаем соединение
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");  // Метод GET для получения данных
            connection.setRequestProperty("X-Yandex-Weather-Key", API_KEY);  // Устанавливаем заголовок с ключом API
            connection.setConnectTimeout(5000);  // Таймаут для соединения
            connection.setReadTimeout(5000);     // Таймаут для чтения данных

            // Читаем ответ от сервера
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            // Читаем данные построчно и добавляем в строку
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();  // Закрываем поток чтения

            // Возвращаем ответ в виде строки
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;  // В случае ошибки возвращаем null
        }
    }

    /**
     * Метод для вычисления средней температуры за определенный период (например, за 5 дней).
     *
     * @param jsonResponse - объект с данными ответа от API
     * @param days - количество дней для расчета средней температуры
     * @return средняя температура за указанный период
     */
    public static double calculateAverageTemperature(JSONObject jsonResponse, int days) {
        // Получаем массив прогнозов по дням из ответа
        JSONArray forecasts = jsonResponse.getJSONArray("forecasts");

        // Переменная для накопления суммы температур
        double sum = 0;

        // Перебираем данные для каждого дня и суммируем среднюю температуру
        for (int i = 0; i < days && i < forecasts.length(); i++) {
            JSONObject dailyForecast = forecasts.getJSONObject(i);  // Получаем данные для одного дня

            // Попробуем извлечь температуру из разных частей дня, например, из "day" или "day_short"
            double dayTemperature;
            try {
                // Пробуем получить среднюю температуру из "day_short"
                dayTemperature = dailyForecast.getJSONObject("parts").getJSONObject("day_short").getDouble("temp_avg");
            } catch (Exception e) {
                // Если не нашли "temp_avg", берем максимальную температуру из "day"
                dayTemperature = dailyForecast.getJSONObject("parts").getJSONObject("day").getDouble("temp_max");
            }

            sum += dayTemperature;  // Добавляем температуру к сумме
        }

        // Вычисляем среднее арифметическое и возвращаем результат
        return sum / days;
    }
}
