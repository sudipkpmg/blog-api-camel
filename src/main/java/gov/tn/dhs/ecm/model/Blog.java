package gov.tn.dhs.ecm.model;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;

@Component
public class Blog {

    private static LinkedHashMap<String, Post> store;

    public Blog() {
        store = new LinkedHashMap<>();
    }

    public static LinkedHashMap<String, Post> getStore() {
        return store;
    }

    public static synchronized Post getPost(String id) {
        return store.get(id);
    }

    public static synchronized void addPost(String id, Post post) {
        store.put(id, post);
    }

    public static synchronized int getSize() {
        return store.size();
    }

}
