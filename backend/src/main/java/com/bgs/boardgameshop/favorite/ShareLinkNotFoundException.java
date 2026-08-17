package com.bgs.boardgameshop.favorite;

public class ShareLinkNotFoundException extends RuntimeException {

    public ShareLinkNotFoundException(String token) {
        super("Aucune liste de favoris partagée pour ce lien : " + token);
    }
}
