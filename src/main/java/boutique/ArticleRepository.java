package boutique;

import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data ecrit l'implementation au demarrage : c'est un bean comme les autres. */
public interface ArticleRepository extends JpaRepository<Article, Long> {
}
