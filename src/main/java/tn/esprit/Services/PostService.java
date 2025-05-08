package tn.esprit.Services;

import tn.esprit.Models.Post;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PostService {
    private List<Post> posts;

    public PostService() {
        // Simuler des publications pour l'exemple
        posts = new ArrayList<>();
        posts.add(new Post("Alice", LocalDateTime.now().minusHours(1), "Just had a great day at the beach! 🏖️"));
        posts.add(new Post("Bob", LocalDateTime.now().minusHours(2), "Working on a new project. Stay tuned! 🚀"));
        posts.add(new Post("Charlie", LocalDateTime.now().minusHours(3), "Does anyone have recommendations for a good sci-fi book? 📚"));
    }

    public List<Post> getPosts() {
        return new ArrayList<>(posts); // Retourne une copie pour éviter les modifications directes
    }

    public void addPost(Post post) {
        posts.add(0, post); // Ajoute au début de la liste (plus récent en haut)
    }
}