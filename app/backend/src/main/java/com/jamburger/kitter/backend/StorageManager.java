package com.jamburger.kitter.backend;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.firebase.cloud.StorageClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StorageManager {
    private static final int NUM_THREADS = 10;
    private static final Bucket bucket = StorageClient.getInstance().bucket();

    public static void deleteAllPosts() {
        ExecutorService executorService = Executors.newFixedThreadPool(NUM_THREADS);
        String folderPath = "Posts";
        Iterable<Blob> blobs = bucket.list(Storage.BlobListOption.prefix(folderPath)).iterateAll();

        for (Blob blob : blobs) {
            executorService.submit(() -> blob.delete());
        }
        executorService.shutdown();
    }
}
