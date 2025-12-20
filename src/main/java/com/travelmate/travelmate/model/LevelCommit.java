package com.travelmate.travelmate.model;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.travelmate.travelmate.firebase.FirebaseService;
import com.travelmate.travelmate.session.UserList;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class LevelCommit {

    private String id;
    private User author;
    private int point;
    private Date date;

    public LevelCommit(String id) throws ExecutionException, InterruptedException {
        this.id = id;
        Firestore db = FirebaseService.getFirestore();
        DocumentSnapshot data = db.collection("levelCommits").document(id).get().get();
        if (data.exists()){
            this.author = UserList.getUser(data.get("author").toString());
            this.point = (int) data.get("point");
            this.date = (Date) data.get("date");
        } else {

        }
    }

    public LevelCommit(String id, User user, int point) {
        this.id = id;
        this.author = user;
        this.point = point;
        this.date = new Date();
        Firestore db = FirebaseService.getFirestore();
        Map<String, Object> data = new HashMap<>();
        data.put("author", author.getId());
        data.put("point", point);
        data.put("date", date.getTime());
        db.collection("levelCommits").document(id).set(data);
    }

    public boolean isInLastMonth(){
        LocalDate localDate = this.date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        LocalDate now = LocalDate.now();
        return YearMonth.from(localDate).equals(YearMonth.from(now));
    }
}
