package audiohub.repository;

import audiohub.entity.ResourceFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ResourceFileRepository extends JpaRepository<ResourceFileEntity, Long> {

    @Query("SELECT r.id FROM ResourceFileEntity r WHERE r.id IN :ids")
    List<Long> findExistingIds(List<Long> ids);
}