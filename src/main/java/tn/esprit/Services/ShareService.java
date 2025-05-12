package tn.esprit.Services;

import tn.esprit.Models.Share;
import tn.esprit.repositories.ShareRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

public class ShareService {
    private static final Logger logger = LoggerFactory.getLogger(ShareService.class);
    private final ShareRepository shareRepository;

    public ShareService() {
        this.shareRepository = new ShareRepository();
    }

    public void sharePost(int postId, Integer userId, Integer sharedFromId) throws SQLException {
        try {
            Share share = new Share();
            share.setPostId(postId);
            share.setUserId(userId);
            share.setSharedFromId(sharedFromId);
            shareRepository.sharePost(share);
            logger.info("Post {} partagé avec succès par l'utilisateur {}", postId, userId);
        } catch (SQLException e) {
            logger.error("Erreur lors du partage du post {} par l'utilisateur {}", postId, userId, e);
            throw e;
        }
    }

    public List<Share> getSharesForPost(int postId) throws SQLException {
        try {
            List<Share> shares = shareRepository.findByPostId(postId);
            logger.debug("Récupéré {} partages pour le post {}", shares.size(), postId);
            return shares;
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération des partages pour le post {}", postId, e);
            throw e;
        }
    }

    public int getShareCount(int postId) throws SQLException {
        try {
            int count = shareRepository.countByPostId(postId);
            logger.debug("Nombre de partages pour le post {} : {}", postId, count);
            return count;
        } catch (SQLException e) {
            logger.error("Erreur lors du comptage des partages pour le post {}", postId, e);
            throw e;
        }
    }

    public void unshare(int shareId, Integer userId) throws SQLException {
        try {
            shareRepository.unshare(shareId, userId);
            logger.info("Partage {} supprimé avec succès pour l'utilisateur {}", shareId, userId);
        } catch (SQLException e) {
            logger.error("Erreur lors de la suppression du partage {} pour l'utilisateur {}", shareId, userId, e);
            throw e;
        }
    }
}