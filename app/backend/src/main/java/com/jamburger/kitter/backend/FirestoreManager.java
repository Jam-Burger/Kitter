package com.jamburger.kitter.backend;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FirestoreManager {
    private static final int NUM_THREADS = 10;
    private static final Firestore db = FirestoreClient.getFirestore();

    public static void manageUsers() throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(NUM_THREADS);
        Query query = db.collection("Users");
        QuerySnapshot querySnapshot = query.get().get();
        for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
            executorService.submit(() -> {
                DocumentReference documentReference = documentSnapshot.getReference();
                Object postsData = documentSnapshot.get("posts");
                List<DocumentReference> postsList = toList(postsData);

                CollectionReference feedReference = documentReference.collection("feed");
                try {
                    for (DocumentReference postReference : postsList) {
                        System.out.println("here!!!");
                        Map<String, Object> map = new HashMap<>();
                        map.put("postReference", postReference);
                        map.put("visited", false);
                        feedReference.document(postReference.getId()).create(map).get();
                    }
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }

            });
        }
        executorService.shutdown();
    }

    public static void deleteAllPosts() throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        for (DocumentSnapshot user : db.collection("Users").get().get().getDocuments()) {
            executorService.submit(() -> user.getReference().update("posts", new ArrayList<DocumentReference>()).get());
            executorService.submit(() -> user.getReference().update("saved", new ArrayList<DocumentReference>()).get());
            for (DocumentSnapshot feed : user.getReference().collection("feed").get().get()) {
                executorService.submit(() -> feed.getReference().delete().get());
            }
        }
        for (DocumentSnapshot post : db.collection("Posts").get().get().getDocuments()) {
            executorService.submit(() -> post.getReference().delete().get());
        }
        executorService.shutdown();
    }

    static List<DocumentReference> toList(Object data) {
        List<DocumentReference> list = new ArrayList<>();
        if (data instanceof List<?>) {
            List<?> objectList = (List<?>) data;
            for (Object obj : objectList) {
                if (obj instanceof DocumentReference) {
                    DocumentReference documentReference = (DocumentReference) obj;
                    list.add(documentReference);
                }
            }
        }
        return list;
    }
}
