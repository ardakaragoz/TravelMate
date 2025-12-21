package com.travelmate.travelmate.session;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.travelmate.travelmate.model.TripTypes;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TripTypeList {
    public static HashMap<String, TripTypes> triptypes = new HashMap<>();

    public static void addTripType(TripTypes type) {
        triptypes.put(type.getName(), type);
    }

    public static TripTypes getTripType(String name) {
        return triptypes.get(name);
    }

    // JSON Karşılığı Ara Sınıf
    public static class TripTypeJson {
        public String name;
        public int funPoint;
        public int culturePoint;
        public int chillPoint;
    }

    public static void listAllTripTypes() {
        try {
            // Dosyayı resources/data klasöründen oku
            InputStream is = TripTypeList.class.getResourceAsStream("/data/trip_types.json");

            if (is == null) {
                System.err.println("HATA: trip_types.json dosyası '/data/' klasöründe bulunamadı!");
                return;
            }

            Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);

            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<TripTypeJson>>(){}.getType();
            List<TripTypeJson> jsonList = gson.fromJson(reader, listType);

            triptypes.clear();
            for (TripTypeJson item : jsonList) {
                int[] scores = new int[3];
                scores[0] = item.funPoint;
                scores[1] = item.culturePoint;
                scores[2] = item.chillPoint;

                // TripTypes constructor yapına göre: (id, name, scores)
                TripTypes tripType = new TripTypes(item.name, item.name, scores);

                addTripType(tripType);
            }

            System.out.println("Başarılı! JSON'dan " + triptypes.size() + " trip type yüklendi.");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("TripTypes JSON'dan yüklenirken hata oluştu.");
        }
    }
}