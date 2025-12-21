package com.travelmate.travelmate.session;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.travelmate.travelmate.model.Hobby;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HobbyList {
    public static HashMap<String, Hobby> hobbies = new HashMap<>();

    public static void addHobby(Hobby hobby) {
        hobbies.put(hobby.getName(), hobby);
    }

    public static Hobby getHobby(String name) {
        return hobbies.get(name);
    }

    // JSON Karşılığı Ara Sınıf
    public static class HobbyJson {
        public String name;
        public int funPoint;
        public int culturePoint;
        public int chillPoint;
    }

    // İsimlendirmeyi diğerleriyle uyumlu olsun diye loadAllHobbies yaptım
    public static void loadAllHobbies() {
        try {
            // Dosyayı resources/data klasöründen oku
            InputStream is = HobbyList.class.getResourceAsStream("/data/hobbies.json");

            if (is == null) {
                System.err.println("HATA: hobbies.json dosyası '/data/' klasöründe bulunamadı!");
                return;
            }

            Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);

            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<HobbyJson>>(){}.getType();
            List<HobbyJson> jsonList = gson.fromJson(reader, listType);

            hobbies.clear();
            for (HobbyJson item : jsonList) {
                int[] scores = new int[3];
                scores[0] = item.funPoint;
                scores[1] = item.culturePoint;
                scores[2] = item.chillPoint;

                // Hobby constructor yapına göre: (id, name, scores)
                Hobby hobby = new Hobby(item.name, item.name, scores);

                addHobby(hobby);
            }

            System.out.println("Başarılı! JSON'dan " + hobbies.size() + " hobi RAM'e yüklendi.");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Hobiler JSON'dan yüklenirken hata oluştu.");
        }
    }
}