package com.bgs.boardgameshop.game;

public record TagResponse(Long id, String name, String slug) {

    public static TagResponse fromEntity(Tag tag) {
        return new TagResponse(tag.getId(), tag.getName(), tag.getSlug());
    }
}
