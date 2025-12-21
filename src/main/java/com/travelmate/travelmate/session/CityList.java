package com.travelmate.travelmate.session;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.travelmate.travelmate.model.City;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CityList {
    public static HashMap<String, City> cities = new HashMap<>();

    public static void addCity(City city) {
        cities.put(city.getName(), city);
    }

    public static City getCity(String name) {
        return cities.get(name);
    }

    // JSON'daki yapıyı karşılayacak ara sınıf (Gson bunun içini dolduracak)
    public static class CityJson {
        public String name;
        public int funPoint;
        public int culturePoint;
        public int chillPoint;
    }

    // ARTIK BU METODU KULLANACAKSIN
    public static void loadAllCities() {
        try {
            // 1. Dosyayı Resources/data klasöründen oku
            // Dosyanın tam yolu: src/main/resources/data/cities.json olmalı
            InputStream is = CityList.class.getResourceAsStream("/data/cities.json");

            if (is == null) {
                System.err.println("HATA: cities.json dosyası '/data/' klasöründe bulunamadı!");
                return;
            }

            Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);

            // 2. Gson ile JSON'ı listeye çevir
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<CityJson>>(){}.getType();
            List<CityJson> jsonList = gson.fromJson(reader, listType);

            // 3. Okunan verileri senin City modeline çevirip HashMap'e at
            cities.clear();
            for (CityJson cj : jsonList) {
                int[] scores = new int[3];
                scores[0] = cj.funPoint;
                scores[1] = cj.culturePoint;
                scores[2] = cj.chillPoint;

                // Senin City constructor'ın: City(String id, String name, String desc, int[] scores)
                // ID ve Name aynı olabilir.
                City city = new City(cj.name, cj.name, "", scores);

                addCity(city);
            }

            System.out.println("Başarılı! JSON'dan " + cities.size() + " şehir RAM'e yüklendi.");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Şehirler JSON'dan yüklenirken hata oluştu.");
        }
    }
}