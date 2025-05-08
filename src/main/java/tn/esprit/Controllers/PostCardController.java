package tn.esprit.Controllers;

import tn.esprit.Models.Post;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.text.Text;

import java.time.format.DateTimeFormatter;

public class PostCardController {
    @FXML private Label authorLabel;
    @FXML private Label timestampLabel;
    @FXML private Text contentText;
    @FXML private Button likeButton;
    @FXML private Button commentButton;
    @FXML private Button shareButton;

    private Post post;

    public void setPost(Post post) {
        this.post = post;
        authorLabel.setText(post.getAuthor());
        timestampLabel.setText(post.getTimestamp().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        contentText.setText(post.getContent());
        updateButtons();
    }

    private void updateButtons() {
        likeButton.setText("Like (" + post.getLikes() + ")");
        commentButton.setText("Comment (" + post.getComments() + ")");
        shareButton.setText("Share (" + post.getShares() + ")");
    }

    @FXML
    private void handleLike() {
        post.like();
        updateButtons();
    }

    @FXML
    private void handleComment() {
        post.addComment();
        updateButtons();
    }

    @FXML
    private void handleShare() {
        post.share();
        updateButtons();
    }
}