package com.bgs.boardgameshop.game;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class TagService {

    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9]+");

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Transactional(readOnly = true)
    public List<TagResponse> getTags() {
        return tagRepository.findAll().stream().map(TagResponse::fromEntity).toList();
    }

    @Transactional
    public TagResponse createTag(String name) {
        String slug = slugify(name);
        if (tagRepository.existsBySlug(slug)) {
            throw new TagAlreadyExistsException(name);
        }

        Tag tag = Tag.builder().name(name).slug(slug).build();
        return TagResponse.fromEntity(tagRepository.save(tag));
    }

    /**
     * Resout une liste d'identifiants de tags en entites, pour rattacher a un jeu.
     */
    @Transactional(readOnly = true)
    public Set<Tag> findByIds(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return new HashSet<>();
        }

        Set<Tag> tags = new HashSet<>(tagRepository.findAllById(tagIds));
        if (tags.size() != new HashSet<>(tagIds).size()) {
            throw new TagNotFoundException(tagIds.get(0));
        }
        return tags;
    }

    private String slugify(String name) {
        String normalized = Normalizer.normalize(name.toLowerCase().trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        String slug = NON_ALPHANUMERIC.matcher(normalized).replaceAll("-");
        return slug.replaceAll("^-+|-+$", "");
    }
}
