package audiohub.repository;

import audiohub.entity.ResourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

public interface ResourceRepository extends JpaRepository<ResourceEntity, Long> {

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM resources WHERE id IN (:ids) RETURNING id", nativeQuery = true)
    Set<Long> deleteAndReturnIds(@Param("ids") Set<Long> ids);
}