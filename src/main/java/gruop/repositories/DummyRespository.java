package gruop.repositories;

import gruop.entities.DummyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DummyRespository extends JpaRepository<DummyEntity,Long> {
}
