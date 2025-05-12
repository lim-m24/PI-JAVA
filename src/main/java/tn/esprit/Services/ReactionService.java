package tn.esprit.Services;

import tn.esprit.Models.Reaction;
import tn.esprit.repositories.ReactionRepository;

import java.sql.SQLException;

public class ReactionService {
    private final ReactionRepository reactionRepository;

    public ReactionService() {
        this.reactionRepository = new ReactionRepository();
    }

    public void toggleReaction(int postId, Integer userId, String reactionType) throws SQLException {
        Reaction reaction = new Reaction();
        reaction.setPostId(postId);
        reaction.setUserId(userId);
        reaction.setType(reactionType);
        reactionRepository.toggleReaction(reaction);
    }

    public int getReactionCount(int postId, String reactionType) throws SQLException {
        return reactionRepository.countByPostAndType(postId, reactionType);
    }

    public boolean hasUserReacted(int postId, int userId) throws SQLException {
        return reactionRepository.existsByUserAndPost(userId, postId);
    }
}